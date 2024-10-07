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

package com.quincyjo.braid

/** The Braid API for JSON values. Provides an API for creating, modifying, and
  * accessing JSON values. Code implemented against this API will be able to
  * operate on any JSON which provides evidence of its Braid.
  * @tparam Json
  *   The JSON type of this Braid.
  */
trait Braid[Json] {

  /** Creates a JSON string with the value of the given fromString.
    * @param string
    *   The strings value of the JSON.
    * @return
    *   A JSON string with the given value.
    */
  def fromString(string: String): Json

  /** Creates a JSON number with the value of the given
    * [[scala.math.BigDecimal]].
    * @param bigDecimal
    *   The value of the JSON.
    * @return
    *   A JSON number with the given value.
    */
  def fromBigDecimal(bigDecimal: BigDecimal): Json

  /** Creates a JSON number with the value of the given [[scala.Int]].
    * @param int
    *   The value of the JSON.
    * @return
    *   A JSON number with the given value.
    */
  def fromInt(int: Int): Json

  /** Creates a JSON number with the value of the given [[scala.BigInt]].
    * @param bigInt
    *   The value of the JSON.
    * @return
    *   A JSON number with the given value.
    */
  def fromBigInt(bigInt: BigInt): Json

  /** Creates a JSON number with the value of the given [[scala.Long]].
    * @param long
    *   The number value of the JSON.
    * @return
    *   A JSON number with the given value.
    */
  def fromLong(long: Long): Json

  /** Creates a JSON number with the value of the given [[scala.Float]]. If the
    * [[scala.Float]] value can not be represented as JSON, then [[scala.None]]
    * is returned.
    * @param float
    *   The number value of the JSON.
    * @return
    *   A JSON number with the given value or None.
    */
  def fromFloat(float: Float): Option[Json]

  /** Creates a JSON number with the value of the given [[scala.Double]]. If the
    * [[scala.Double]] value can not be represented as JSON, then [[scala.None]]
    * is returned.
    * @param double
    *   The number value of the JSON.
    * @return
    *   A JSON number with the given value or None.
    */
  def fromDouble(double: Double): Option[Json]

  /** Creates a JSON boolean with the value of the given boolean.
    * @param boolean
    *   The boolean value of the JSON.
    * @return
    *   A JSON boolean with the given value.
    */
  def fromBoolean(boolean: Boolean): Json

  /** Creates a JSON array containing the given JSON values.
    * @param json
    *   The JSON values to be contained within the JSON array.
    * @return
    *   A JSON array containing the given values.
    */
  def arr(json: Json*): Json

  /** Creates a JSON object containing the given key value pairs.
    * @param field
    *   The key value pairs to be contained within the JSON object.
    * @return
    *   A JSON object containing the given key value pairs.
    */
  def obj(field: (String, Json)*): Json

  /** Creates a JSON array of provided iterable.
    * @param values
    *   An iterable containing the JSON values to be contained within the JSON
    *   array.
    * @return
    *   A JSON array representing the given iterable.
    */
  def fromValues(values: Iterable[Json]): Json

  /** Creates a JSON object of provided iterable.
    * @param fields
    *   An iterable containing the key value pairs.
    * @return
    *   A JSON object representing the given iterable.
    */
  def fromFields(fields: Iterable[(String, Json)]): Json

  /** Representation of a JSON null.
    * @return
    *   A JSON null.
    */
  def Null: Json

  /** Checks if the given JSON is an object.
    * @param json
    *   The JSON to evaluate.
    * @return
    *   True if the JSON is an object, false otherwise.
    */
  def isObject(json: Json): Boolean

  /** Checks if the given JSON is an array.
    * @param json
    *   The JSON to evaluate.
    * @return
    *   True if the JSON is an array, false otherwise.
    */
  def isArray(json: Json): Boolean

  /** Checks if the given JSON is a fromString.
    * @param json
    *   The JSON to evaluate.
    * @return
    *   True if the JSON is a fromString, false otherwise.
    */
  def isString(json: Json): Boolean

  /** Checks if the given JSON is a boolean.
    * @param json
    *   The JSON to evaluate.
    * @return
    *   True if the JSON is a boolean, false otherwise.
    */
  def isBoolean(json: Json): Boolean

  /** Checks if the given JSON is a number.
    * @param json
    *   The JSON to evaluate.
    * @return
    *   True if the JSON is a number, false otherwise.
    */
  def isNumber(json: Json): Boolean

  /** Checks if the given JSON is null.
    * @param json
    *   The JSON to evaluate.
    * @return
    *   True if the JSON is null, false otherwise.
    */
  def isNull(json: Json): Boolean

  /** Returns a [[scala.collection.Map]] representation of the JSON if it is an
    * object or None otherwise.
    * @param json
    *   The JSON to evaluate.
    * @return
    *   A [[scala.collection.Map]] of key value pairs or None.
    */
  def asObject(json: Json): Option[Map[String, Json]]

  /** Returns a [[scala.collection.immutable.Vector]] representation of the JSON
    * if it is an array or None otherwise.
    * @param json
    *   The JSON to evaluate.
    * @return
    *   A [[scala.collection.immutable.Vector]] of JSON values or None.
    */
  def asArray(json: Json): Option[Vector[Json]]

  /** Returns a fromString if the JSON in a fromString or None otherwise.
    * @param json
    *   The JSON to evaluate.
    * @return
    *   A fromString or None.
    */
  def asString(json: Json): Option[String]

  /** Returns a boolean if the JSON is a boolean or None otherwise.
    * @param json
    *   The JSON to evaluate.
    * @return
    *   A boolean or None.
    */
  def asBoolean(json: Json): Option[Boolean]

  /** Returns a number if the JSON is a number or None otherwise.
    * @param json
    *   The JSON to evaluate.
    * @return
    *   A number or None.
    */
  def asNumber(json: Json): Option[BigDecimal]

  /** Returns [[scala.Unit]] if the JSON is null or None otherwise.
    * @param json
    *   The JSON to evaluate.
    * @return
    *   A [[scala.Unit]] or None.
    */
  def asNull(json: Json): Option[Unit]

  /** Applies the given function to the JSON if it is an object or returns the
    * JSON unmodified otherwise.
    * @param json
    *   The JSON to evaluate.
    * @param f
    *   The function to apply to the JSON if it is an object.
    * @return
    *   The transformed object or original JSON if not an object.
    */
  def mapObject(json: Json)(f: Map[String, Json] => Map[String, Json]): Json

  /** Applies the given function to the JSON if it is an array or returns the
    * JSON unmodified otherwise.
    * @param json
    *   The JSON to evaluate.
    * @param f
    *   The function to apply to the JSON if it is an array.
    * @return
    *   The transformed array or original JSON if not an array.
    */
  def mapArray(json: Json)(f: Vector[Json] => Vector[Json]): Json

  /** Applies the given function to the JSON if it is a fromString or returns
    * the JSON unmodified otherwise.
    * @param json
    *   The JSON to evaluate.
    * @param f
    *   The function to apply to the JSON if it is a fromString.
    * @return
    *   The transformed fromString or original JSON if not a fromString.
    */
  def mapString(json: Json)(f: String => String): Json

  /** Applies the given function to the JSON if it is a boolean or returns the
    * JSON unmodified otherwise.
    * @param json
    *   The JSON to evaluate.
    * @param f
    *   The function to apply to the JSON if it is a boolean.
    * @return
    *   The transformed boolean or original JSON if not a boolean.
    */
  def mapBoolean(json: Json)(f: Boolean => Boolean): Json

  /** Applies the given function to the JSON if it is a number or returns the
    * JSON unmodified otherwise.
    * @param json
    *   The JSON to evaluate.
    * @param f
    *   The function to apply to the JSON if it is a number.
    * @return
    *   The transformed number or original JSON if not a number.
    */
  def mapNumber(json: Json)(f: BigDecimal => BigDecimal): Json

  /** Reduces this JSON with the given functions.
    * @param json
    *   The JSON to reduce.
    * @param ifNull
    *   If the JSON is null, return this value.
    * @param jsonBoolean
    *   If the JSON is a boolean, it is transformed by this.
    * @param jsonNumber
    *   If the JSON is a number, it is transformed by this.
    * @param jsonString
    *   If the JSON is a fromString, it is transformed by this.
    * @param jsonArray
    *   If the JSON is an array, it is transformed by this.
    * @param jsonObject
    *   If the JSON is an object, it is transformed by this.
    * @tparam B
    *   The reduced type.
    * @return
    *   The reduced value.
    */
  def fold[B](json: Json)(
      ifNull: => B,
      jsonBoolean: Boolean => B,
      jsonNumber: BigDecimal => B,
      jsonString: String => B,
      jsonArray: Vector[Json] => B,
      jsonObject: Map[String, Json] => B
  ): B

  /** Returns true if the JSON is an atomic value. IE, a fromString, number,
    * boolean, or null.
    * @param json
    *   The JSON to evaluate.
    * @return
    *   True if the JSON is an atomic value, false if not.
    */
  final def isAtomic(json: Json): Boolean = !isObject(json) && !isArray(json)

  /** Returns true if the JSON is associative. IE, an object or an array.
    * @param json
    *   The JSON to evaluate.
    * @return
    *   True if the JSON is associative, false if not.
    */
  final def isAssociative(json: Json): Boolean = isObject(json) || isArray(json)

  /** Reduces this JSON if it is an object or an array, or returns {@code
    * orElse} otherwise.
    * @param json
    *   The JSON to reduce.
    * @param orElse
    *   The value to return if the JSON is not an object or an array.
    * @param jsonArray
    *   If the JSON is an array, it is transformed by this.
    * @param jsonObject
    *   If the JSON is an object, it is transformed by this.
    * @tparam B
    *   The reduced type.
    * @return
    *   The reduced value.
    */
  final def arrayOrObject[B](json: Json)(
      orElse: => B,
      jsonArray: Vector[Json] => B,
      jsonObject: Map[String, Json] => B
  ): B = fold(json)(
    orElse,
    _ => orElse,
    _ => orElse,
    _ => orElse,
    jsonArray,
    jsonObject
  )
}

object Braid {

  trait ToBraidedJsonOps {

    implicit def toBraidedJson[Json](json: Json)(implicit
        braid: Braid[Json]
    ): Braid.BraidedJsonOps[Json] =
      new Braid.BraidedJsonOps[Json](json)
  }

  object ToBraidedJsonOps extends ToBraidedJsonOps

  /** Provides the [[Braid]] API as members of the wrapped JSON value.
    * @param json
    *   The JSON value to wrap.
    * @param braid
    *   The [[Braid]] for the type of {@code json}.
    * @tparam Json
    *   The type of {@code json}.
    */
  class BraidedJsonOps[Json](json: Json)(implicit
      braid: Braid[Json]
  ) {

    def isObject: Boolean = braid.isObject(json)
    def isArray: Boolean = braid.isArray(json)
    def isString: Boolean = braid.isString(json)
    def isBoolean: Boolean = braid.isBoolean(json)
    def isNumber: Boolean = braid.isNumber(json)
    def isNull: Boolean = braid.isNull(json)

    def asObject: Option[Map[String, Json]] = braid.asObject(json)
    def asArray: Option[Vector[Json]] = braid.asArray(json)
    def asString: Option[String] = braid.asString(json)
    def asBoolean: Option[Boolean] = braid.asBoolean(json)
    def asNumber: Option[BigDecimal] = braid.asNumber(json)
    def asNull: Option[Unit] = braid.asNull(json)

    def mapObject(f: Map[String, Json] => Map[String, Json]): Json =
      braid.mapObject(json)(f)

    def mapArray(f: Vector[Json] => Vector[Json]): Json =
      braid.mapArray(json)(f)

    def mapString(f: String => String): Json =
      braid.mapString(json)(f)

    def mapBoolean(f: Boolean => Boolean): Json =
      braid.mapBoolean(json)(f)

    def mapNumber(f: BigDecimal => BigDecimal): Json =
      braid.mapNumber(json)(f)

    def isAtomic: Boolean = braid.isAtomic(json)
    def isAssociative: Boolean = braid.isAssociative(json)

    def fold[B](
        ifNull: => B,
        jsonBoolean: Boolean => B,
        jsonNumber: BigDecimal => B,
        jsonString: String => B,
        jsonArray: Vector[Json] => B,
        jsonObject: Map[String, Json] => B
    ): B = braid.fold(json)(
      ifNull,
      jsonBoolean,
      jsonNumber,
      jsonString,
      jsonArray,
      jsonObject
    )

    def arrayOrObject[B](
        orElse: => B,
        jsonArray: Vector[Json] => B,
        jsonObject: Map[String, Json] => B
    ): B = braid.arrayOrObject(json)(orElse, jsonArray, jsonObject)
  }
}
