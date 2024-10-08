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

package com.quincyjo.braid.play

import com.quincyjo.braid.Braid
import play.api.libs.json._

object PlayJsonBraid extends Braid[JsValue] {

  override def fromString(string: String): JsValue =
    JsString(string)

  override def fromBigDecimal(bigDecimal: BigDecimal): JsValue =
    JsNumber(bigDecimal)

  override def fromInt(int: Int): JsValue =
    JsNumber(BigDecimal(int))

  override def fromBigInt(bigInt: BigInt): JsValue =
    JsNumber(BigDecimal(bigInt))

  override def fromLong(long: Long): JsValue =
    JsNumber(BigDecimal(long))

  override def fromFloat(float: Float): Option[JsValue] =
    Option.unless(float.isNaN)(JsNumber(BigDecimal(float.toDouble)))

  override def fromDouble(double: Double): Option[JsValue] =
    Option.unless(double.isNaN)(JsNumber(BigDecimal(double)))

  override def fromBoolean(boolean: Boolean): JsValue =
    JsBoolean(boolean)

  override def arr(json: JsValue*): JsValue =
    JsArray(json)

  override def obj(field: (String, JsValue)*): JsValue =
    JsObject(field.toMap)

  override def fromValues(values: Iterable[JsValue]): JsValue =
    JsArray(values.toSeq)

  override def fromFields(fields: Iterable[(String, JsValue)]): JsValue =
    JsObject(fields.toMap)

  override def Null: JsValue =
    JsNull

  override def asObject(json: JsValue): Option[Map[String, JsValue]] =
    json match {
      case JsObject(underlying) => Option(underlying.toMap)
      case _                    => None
    }

  override def asArray(json: JsValue): Option[Vector[JsValue]] =
    json match {
      case JsArray(values) => Some(values.toVector)
      case _               => None
    }

  override def asString(json: JsValue): Option[String] =
    json match {
      case JsString(value) => Some(value)
      case _               => None
    }

  override def asBoolean(json: JsValue): Option[Boolean] =
    json match {
      case boolean: JsBoolean => Some(boolean.value)
      case _                  => None
    }

  override def asNumber(json: JsValue): Option[BigDecimal] =
    json match {
      case JsNumber(value) => Some(value)
      case _               => None
    }

  override def asNull(json: JsValue): Option[Unit] =
    json match {
      case JsNull => Some(())
      case _      => None
    }

  override def isObject(json: JsValue): Boolean =
    json match {
      case JsObject(_) => true
      case _           => false
    }

  override def isArray(json: JsValue): Boolean =
    json match {
      case JsArray(_) => true
      case _          => false
    }

  override def isString(json: JsValue): Boolean =
    json match {
      case JsString(_) => true
      case _           => false
    }

  override def isBoolean(json: JsValue): Boolean =
    json match {
      case JsBoolean(_) => true
      case _            => false
    }

  override def isNumber(json: JsValue): Boolean =
    json match {
      case JsNumber(_) => true
      case _           => false
    }

  override def isNull(json: JsValue): Boolean =
    json match {
      case JsNull => true
      case _      => false
    }

  override def fold[B](json: JsValue)(
      ifNull: => B,
      jsonBoolean: Boolean => B,
      jsonNumber: BigDecimal => B,
      jsonString: String => B,
      jsonArray: Vector[JsValue] => B,
      jsonObject: Map[String, JsValue] => B
  ): B = json match {
    case JsNull               => ifNull
    case boolean: JsBoolean   => jsonBoolean(boolean.value)
    case JsNumber(value)      => jsonNumber(value)
    case JsString(value)      => jsonString(value)
    case JsArray(value)       => jsonArray(value.toVector)
    case JsObject(underlying) => jsonObject(underlying.toMap)
  }

  override def mapObject(json: JsValue)(
      f: Map[String, JsValue] => Map[String, JsValue]
  ): JsValue =
    json match {
      case JsObject(underlying) => JsObject(f(underlying.toMap))
      case other                => other
    }

  override def mapArray(json: JsValue)(
      f: Vector[JsValue] => Vector[JsValue]
  ): JsValue =
    json match {
      case JsArray(values) => JsArray(f(values.toVector))
      case other           => other
    }

  override def mapString(json: JsValue)(f: String => String): JsValue = {
    json match {
      case JsString(value) => JsString(f(value))
      case other           => other
    }
  }

  override def mapBoolean(json: JsValue)(f: Boolean => Boolean): JsValue =
    json match {
      case JsBoolean(value) => JsBoolean(f(value))
      case other            => other
    }

  override def mapNumber(json: JsValue)(f: BigDecimal => BigDecimal): JsValue =
    json match {
      case JsNumber(value) => JsNumber(f(value))
      case other           => other
    }
}
