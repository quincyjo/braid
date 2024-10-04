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

  override def asNumber(json: JValue): Option[BigDecimal] =
    json match {
      case JDecimal(num)              => Some(num)
      case JInt(num)                  => Some(BigDecimal(num))
      case JLong(num)                 => Some(BigDecimal(num))
      case JDouble(num) if !num.isNaN => Some(BigDecimal(num))
      case _                          => None
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
      jsonNumber: BigDecimal => B,
      jsonString: String => B,
      jsonArray: Vector[JValue] => B,
      jsonObject: Map[String, JValue] => B
  ): B = json match {
    case JNull | JNothing => ifNull
    case JString(s)       => jsonString(s)
    case JDouble(num) =>
      if (num.isNaN) ifNull else jsonNumber(BigDecimal(num))
    case JDecimal(num) => jsonNumber(num)
    case JLong(num)    => jsonNumber(BigDecimal(num))
    case JInt(num)     => jsonNumber(BigDecimal(num))
    case JBool(value)  => jsonBoolean(value)
    case JObject(obj)  => jsonObject(obj.toMap)
    case JArray(arr)   => jsonArray(arr.toVector)
    case JSet(set)     => jsonArray(set.toVector)
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

  override def mapNumber(json: JValue)(f: BigDecimal => BigDecimal): JValue =
    asNumber(json).map(f).map(fromBigDecimal).getOrElse(json)
}
