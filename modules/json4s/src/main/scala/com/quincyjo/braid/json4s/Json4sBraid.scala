/*
 * Copyright 2024 Quincy Jo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.quincyjo.braid.json4s

import com.quincyjo.braid.Braid
import org.json4s._
import com.quincyjo.braid.numbers.JsonNumber

object Json4sBraid extends Braid[JValue] {

  override def fromString(string: String): JValue =
    JString(string)

  override def fromBigDecimal(bigDecimal: BigDecimal): JValue =
    JDecimal(bigDecimal)

  override def fromInt(int: Int): JValue =
    JInt(int)

  override def fromBigInt(bigInt: BigInt): JValue =
    JDecimal(BigDecimal(bigInt))

  override def fromLong(long: Long): JValue =
    JLong(long)

  override def fromFloat(float: Float): Option[JValue] =
    Option.unless(float.isNaN)(JDouble(float.doubleValue()))

  override def fromDouble(double: Double): Option[JValue] =
    Option.unless(double.isNaN)(JDouble(double))

  override def fromBoolean(boolean: Boolean): JValue =
    JBool(boolean)

  override def arr(json: JValue*): JValue =
    JArray(json.toList)

  override def obj(field: (String, JValue)*): JValue =
    JObject(field.toList)

  override def fromValues(values: Iterable[JValue]): JValue =
    JArray(values.toList)

  override def fromFields(fields: Iterable[(String, JValue)]): JValue =
    JObject(fields.toList)

  override def Null: JValue =
    JNull

  override def asObject(json: JValue): Option[Map[String, JValue]] =
    json match {
      case JObject(obj) => Some(obj.toMap)
      case _            => None
    }

  override def asArray(json: JValue): Option[Vector[JValue]] =
    json match {
      case JArray(arr) => Some(arr.toVector)
      case _           => None
    }

  override def asString(json: JValue): Option[String] =
    json match {
      case JString(str) => Some(str)
      case _            => None
    }

  override def asBoolean(json: JValue): Option[Boolean] =
    json match {
      case JBool(bool) => Some(bool)
      case _           => None
    }

  override def asNumber(json: JValue): Option[JsonNumber[JValue]] =
    Option(json).collect {
      case num: JDecimal                          => Json4sNumber(num)
      case num: JInt                              => Json4sNumber(num)
      case num: JLong                             => Json4sNumber(num)
      case num @ JDouble(double) if !double.isNaN => Json4sNumber(num)
    }

  override def asNull(json: JValue): Option[Unit] =
    json match {
      case JNull => Some(())
      case _     => None
    }

  override def isObject(json: JValue): Boolean =
    json match {
      case _: JObject => true
      case _          => false
    }

  override def isArray(json: JValue): Boolean =
    json match {
      case _: JArray => true
      case _         => false
    }

  override def isString(json: JValue): Boolean =
    json match {
      case _: JString => true
      case _          => false
    }

  override def isBoolean(json: JValue): Boolean =
    json match {
      case _: JBool => true
      case _        => false
    }

  override def isNumber(json: JValue): Boolean =
    json match {
      case _: JNumber => true
      case _          => false
    }

  override def isNull(json: JValue): Boolean =
    json match {
      case JNull => true
      case _     => false
    }

  override def fold[B](json: JValue)(
      ifNull: => B,
      jsonBoolean: Boolean => B,
      jsonNumber: JsonNumber[JValue] => B,
      jsonString: String => B,
      jsonArray: Vector[JValue] => B,
      jsonObject: Map[String, JValue] => B
  ): B = json match {
    case JNull | JNothing => ifNull
    case JString(s)       => jsonString(s)
    case num: JNumber     => jsonNumber(Json4sNumber(num))
    case JBool(value)     => jsonBoolean(value)
    case JObject(obj)     => jsonObject(obj.toMap)
    case JArray(arr)      => jsonArray(arr.toVector)
    case JSet(set)        => jsonArray(set.toVector)
  }

  override def mapObject(json: JValue)(
      f: Map[String, JValue] => Map[String, JValue]
  ): JValue =
    asObject(json).map(f).map(fromFields).getOrElse(json)

  override def mapArray(json: JValue)(
      f: Vector[JValue] => Vector[JValue]
  ): JValue =
    asArray(json).map(f).map(fromValues).getOrElse(json)

  override def mapString(json: JValue)(f: String => String): JValue =
    asString(json).map(f).map(fromString).getOrElse(json)

  override def mapBoolean(json: JValue)(f: Boolean => Boolean): JValue =
    asBoolean(json).map(f).map(fromBoolean).getOrElse(json)

  private final case class Json4sNumber(value: JValue & JNumber)
      extends JsonNumber[JValue] {

    override def asJson: JValue = value

    override def toBigDecimal: Option[BigDecimal] = value match {
      case JInt(bigInt)      => Some(BigDecimal(bigInt))
      case JLong(long)       => Some(BigDecimal(long))
      case JDecimal(decimal) => Some(decimal)
      case JDouble(double)   => Option.when(double.isFinite)(BigDecimal(double))
      case _                 => None
    }

    override def toBigInt: Option[BigInt] = value match {
      case JInt(bigInt)      => Some(bigInt)
      case JLong(long)       => Some(BigInt(long))
      case JDecimal(decimal) => Option.when(decimal.isWhole)(decimal.toBigInt)
      case JDouble(double) =>
        Option
          .when(double.isFinite)(BigDecimal(double))
          .filter(_.isWhole)
          .map(_.toBigInt)
      case _ => None
    }

    override def toDouble: Double = value match {
      case JInt(bigInt)      => bigInt.toDouble
      case JLong(long)       => long.toDouble
      case JDecimal(decimal) => decimal.toDouble
      case JDouble(double)   => double
      case _                 => Double.NaN
    }

    override def toFloat: Float = value match {
      case JInt(bigInt)      => bigInt.toFloat
      case JLong(long)       => long.toFloat
      case JDecimal(decimal) => decimal.toFloat
      case JDouble(double)   => double.toFloat
      case _                 => Float.NaN
    }

    override def toLong: Option[Long] = value match {
      case JInt(bigInt)      => Option.when(bigInt.isValidLong)(bigInt.toLong)
      case JLong(long)       => Some(long)
      case JDecimal(decimal) => Option.when(decimal.isValidLong)(decimal.toLong)
      case JDouble(double) =>
        Option.when {
          val l = double.toLong
          l.toDouble == double && l != Long.MaxValue
        }(double.toLong)
      case _ => None
    }
  }
}
