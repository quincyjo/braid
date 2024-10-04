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

package com.quincyjo.braid.operations

import com.quincyjo.braid.Braid

import scala.util.Try

/** Provides Javascript operations for braided JSONs which evaluate according to
  * Javascript rules.
  * @param braid
  *   The [[com.quincyjo.braid.Braid]] for {@code Json}.
  * @tparam Json
  *   The type of {@code Json}.
  */
trait JsonOperations {

  /** Coerces the given JSON right into a fromBigDecimal. If the result is
    * [[scala.None]], then the right is not a fromBigDecimal (javascript NaN).
    * @param json
    *   The json to coerce.
    * @return
    *   The coerced fromBigDecimal or NaN.
    */
  def coerceToNumber[Json](json: Json)(implicit
      braid: Braid[Json]
  ): Option[BigDecimal] =
    braid.fold(json)(
      Some(0),
      boolean => Some(if (boolean) 1 else 0),
      Some(_),
      string =>
        Option
          .when(string.forall(_.isWhitespace))(BigDecimal(0))
          .orElse(
            Try(BigDecimal(string.trim)).toOption
          ),
      {
        case arr if arr.isEmpty => Some(0)
        case arr if arr.size == 1 =>
          arr.headOption.flatMap(coerceToNumber(_)(braid))
        case _ => None
      },
      _ => None
    )

  /** Coerces the given JSON right into a fromString.
    * @param json
    *   The json to coerce.
    * @return
    *   The coerced fromString.
    */
  def coerceToString[Json](json: Json)(implicit braid: Braid[Json]): String =
    braid.fold(json)(
      "null",
      if (_) "true" else "false",
      _.toString(),
      identity,
      _.map(coerceToString(_)(braid)).mkString(","),
      _ => "[object Object]"
    )

  /** Coerces the given JSON right into a boolean.
    * @param json
    *   The json to coerce.
    * @return
    *   The coerced boolean.
    */
  def coerceToBoolean[Json](json: Json)(implicit braid: Braid[Json]): Boolean =
    braid.fold(json)(
      false,
      identity,
      _ != 0,
      _.nonEmpty,
      _ => true,
      _ => true
    )

  /** If the given JSON is an associative, coerce it into a primitive. Else, the
    * JSON is returned unchanged. There is no preferred target primitive type
    * for this coercion, but this function uses [[coerceToString]].
    * @param json
    *   The JSON to coerce.
    * @return
    *   The coerced JSON if associative, or the original JSON otherwise.
    */
  def coerceToPrimitive[Json](json: Json)(implicit braid: Braid[Json]): Json =
    if (braid.isAssociative(json)) braid.fromString(coerceToString(json))
    else json

  /** Compares the types of the given JSON values, returning true if they are
    * the same are false otherwise. Both arrays and objects are considered
    * objects for the purpose of this comparison.
    * @param a
    *   The first JSON.
    * @param b
    *   The second JSON.
    * @return
    *   True if the types are the same, false otherwise.
    */
  def areSameType[Json](a: Json, b: Json)(implicit
      braid: Braid[Json]
  ): Boolean =
    braid.fold(a)(
      braid.isNull(b),
      _ => braid.isBoolean(b),
      _ => braid.isNumber(b),
      _ => braid.isString(b),
      _ => braid.isAssociative(b),
      _ => braid.isAssociative(b)
    )

  /** Applies type conversion to coerce both of the given JSON values into the
    * same type. If they cannot be coerced into the same type, meaning that one
    * or both of the values produces NaN during the conversion, then nothing is
    * returned. Otherwise, two JSONs of directly comparable type sare returned.
    * @param a
    *   The first JSON.
    * @param b
    *   The second JSON.
    * @return
    *   {@code a} and {@code b} coerced into the same type if able.
    */
  def convertTypes[Json](a: Json, b: Json)(implicit
      braid: Braid[Json]
  ): Option[(Json, Json)] =
    if (areSameType(a, b) || braid.isNull(a) || braid.isNull(b))
      Some(a -> b)
    else {
      val newA =
        if (braid.isAssociative(a)) coerceToPrimitive(a)
        else a
      val newB =
        if (braid.isAssociative(b)) coerceToPrimitive(b)
        else b
      if (areSameType(newA, newB))
        Some(newA -> newB)
      else
        coerceToNumber(newA)
          .zip(coerceToNumber(newB))
          .map { case (a, b) =>
            braid.fromBigDecimal(a) -> braid.fromBigDecimal(b)
          }
    }

  def greaterThan[Json](a: Json, b: Json)(implicit braid: Braid[Json]): Json =
    compare(a, b)(_ > _)

  def greaterThanOrEqualTo[Json](a: Json, b: Json)(implicit
      braid: Braid[Json]
  ): Json =
    compare(a, b)(_ >= _)

  def lessThan[Json](a: Json, b: Json)(implicit braid: Braid[Json]): Json =
    compare(a, b)(_ < _)

  def lessThanOrEqualTo[Json](a: Json, b: Json)(implicit
      braid: Braid[Json]
  ): Json =
    compare(a, b)(_ <= _)

  def equal[Json](a: Json, b: Json)(implicit braid: Braid[Json]): Json =
    braid.fromBoolean(looseEquality(a, b))

  def notEqual[Json](a: Json, b: Json)(implicit braid: Braid[Json]): Json =
    braid.fromBoolean(!looseEquality(a, b))

  def and[Json](a: Json, b: Json)(implicit braid: Braid[Json]): Json =
    braid.fromBoolean(
      coerceToBoolean(a) && coerceToBoolean(b)
    )

  def or[Json](a: Json, b: Json)(implicit braid: Braid[Json]): Json =
    braid.fromBoolean(
      coerceToBoolean(a) || coerceToBoolean(b)
    )

  def plus[Json](
      a: Json,
      b: Json
  )(implicit braid: Braid[Json]): Json = // TODO: {} + 1 == 1
    if (
      (braid.isNumber(a) || braid.isNull(a)) &&
      (braid.isNumber(b) || braid.isNull(b))
    ) arithmetic(a, b)(_ + _)
    else
      braid.fromString(
        coerceToString(a) concat coerceToString(b)
      )

  def minus[Json](a: Json, b: Json)(implicit braid: Braid[Json]): Json =
    arithmetic(a, b)(_ - _)

  def divide[Json](a: Json, b: Json)(implicit braid: Braid[Json]): Json =
    arithmetic(a, b)(_ / _)

  def multiply[Json](a: Json, b: Json)(implicit braid: Braid[Json]): Json =
    arithmetic(a, b)(_ * _)

  private def compare[Json](a: Json, b: Json)(
      f: (Int, Int) => Boolean
  )(implicit braid: Braid[Json]): Json =
    braid.fromBoolean(
      braid
        .asString(a)
        .orElse(Option.when(braid.isArray(a))(coerceToString(a)))
        .zip(
          braid
            .asString(b)
            .orElse(Option.when(braid.isArray(b))(coerceToString(b)))
        )
        .map { case (l, r) => l compareTo r }
        .orElse(
          coerceToNumber(a)
            .zip(coerceToNumber(b))
            .map { case (l, r) => l compareTo r }
        )
        .fold(false)(f(_, 0))
    )

  // TODO: NaN
  private def arithmetic[Json](a: Json, b: Json)(
      f: (BigDecimal, BigDecimal) => BigDecimal
  )(implicit braid: Braid[Json]): Json =
    coerceToNumber(a)
      .zip(coerceToNumber(b))
      .map { case (left, right) =>
        braid.fromBigDecimal(f(left, right))
      }
      .getOrElse(braid.Null)

  private def looseEquality[Json](a: Json, b: Json)(implicit
      braid: Braid[Json]
  ): Boolean =
    if (braid.isAssociative(a) && braid.isAssociative(b)) false
    else
      convertTypes(a, b)
        .fold(false) { case (l, r) =>
          l == r
        }
}

object JsonOperations extends JsonOperations {

  trait ToJsonOperationOps {

    implicit def toJsonOperationOps[Json](json: Json)(implicit
        braid: Braid[Json]
    ): JsonOperations.JsonOperationsOps[Json] =
      new JsonOperations.JsonOperationsOps[Json](json)
  }

  object ToJsonOperationOps extends ToJsonOperationOps

  class JsonOperationsOps[Json](json: Json)(implicit
      braid: Braid[Json]
  ) {

    def coerceToNumber: Option[BigDecimal] =
      JsonOperations.coerceToNumber(json)

    def coerceToString: String =
      JsonOperations.coerceToString(json)

    def coerceToBoolean: Boolean =
      JsonOperations.coerceToBoolean(json)

    def coerceToPrimitive: Json =
      JsonOperations.coerceToPrimitive(json)

    def isSameType(that: Json): Boolean =
      JsonOperations.areSameType(json, that)

    def ~+~(that: Json): Json =
      JsonOperations.plus(json, that)

    def ~-~(that: Json): Json =
      JsonOperations.minus(json, that)

    def ~/~(that: Json): Json =
      JsonOperations.divide(json, that)

    def ~*~(that: Json): Json =
      JsonOperations.multiply(json, that)

    def ~>~(that: Json): Json =
      JsonOperations.greaterThan(json, that)

    def ~>=~(that: Json): Json =
      JsonOperations.greaterThanOrEqualTo(json, that)

    def ~<~(that: Json): Json =
      JsonOperations.lessThan(json, that)

    def ~<=~(that: Json): Json =
      JsonOperations.lessThanOrEqualTo(json, that)

    def ~==~(that: Json): Json =
      JsonOperations.equal(json, that)

    def ~!=~(that: Json): Json =
      JsonOperations.notEqual(json, that)

    def ~&&~(that: Json): Json =
      JsonOperations.and(json, that)

    def ~||~(that: Json): Json =
      JsonOperations.or(json, that)
  }
}
