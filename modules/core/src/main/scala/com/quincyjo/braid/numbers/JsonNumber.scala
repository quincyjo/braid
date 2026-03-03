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

package com.quincyjo.braid.numbers

import com.quincyjo.braid.Braid

/** Represents an instance of an underlying JSON number type.
  *
  * This class provides a common interface for all JSON number implementations.
  * A concrete instance is defined as part of the Braid for a supported library.
  *
  * @tparam Json
  *   The JSON type of this Braid.
  */
abstract class JsonNumber[Json] {

  def asJson: Json

  /** Return this number as a [[scala.math.BigDecimal]].
    */
  def toBigDecimal: Option[BigDecimal]

  /** Return this number as a [[scala.math.BigInt]] if it's a sufficiently small
    * whole number.
    */
  def toBigInt: Option[BigInt]

  /** Convert this number to its best [[scala.Double]] approximation.
    *
    * Anything over `Double.MaxValue` will be rounded to
    * `Double.PositiveInfinity` and anything below `Double.MinValue` is rounded
    * to `Double.NegativeInfinity`.
    */
  def toDouble: Double

  /** Convert this number to its best [[scala.Float]] approximation.
    *
    * Anything over `Float.MaxValue` will be rounded to `Float.PositiveInfinity`
    * and anything below `Float.MinValue` is rounded to
    * `Float.NegativeInfinity`.
    */
  def toFloat: Float

  /** Return this number as a [[scala.Byte]] if it's a valid [[scala.Byte]].
    */
  final def toByte: Option[Byte] = toLong match {
    case Some(n) =>
      val asByte: Byte = n.toByte
      if (n == asByte) Some(asByte) else None
    case None => None
  }

  /** Return this number as a [[scala.Short]] if it's a valid [[scala.Short]].
    */
  final def toShort: Option[Short] = toLong match {
    case Some(n) =>
      val asShort: Short = n.toShort
      if (n == asShort) Some(asShort) else None
    case None => None
  }

  /** Return this number as an [[scala.Int]] if it's a valid [[scala.Int]].
    */
  final def toInt: Option[Int] = toLong match {
    case Some(n) =>
      val asInt: Int = n.toInt
      if (n == asInt) Some(asInt) else None
    case None => None
  }

  /** Return this number as a [[scala.Long]] if it's a valid [[scala.Long]].
    */
  def toLong: Option[Long]
}

object JsonNumber {

  sealed trait JsonNumberMagnet[T] {

    def apply[Json: Braid](t: T): Json
  }

  object JsonNumberMagnet {

    implicit case object IntMagnet extends JsonNumberMagnet[Int] {
      override def apply[Json: Braid](t: Int): Json =
        Braid[Json].fromInt(t)
    }

    implicit case object BigIntMagnet extends JsonNumberMagnet[BigInt] {
      override def apply[Json: Braid](t: BigInt): Json =
        Braid[Json].fromBigInt(t)
    }

    implicit case object LongMagnet extends JsonNumberMagnet[Long] {
      override def apply[Json: Braid](t: Long): Json =
        Braid[Json].fromLong(t)
    }

    implicit case object FloatMagnet extends JsonNumberMagnet[Float] {
      override def apply[Json: Braid](t: Float): Json =
        Braid[Json].fromFloat(t).getOrElse(Braid[Json].Null)
    }

    implicit case object DoubleMagnet extends JsonNumberMagnet[Double] {
      override def apply[Json: Braid](t: Double): Json =
        Braid[Json].fromDouble(t).getOrElse(Braid[Json].Null)
    }

    implicit case object BigDecimalMagnet extends JsonNumberMagnet[BigDecimal] {
      override def apply[Json: Braid](t: BigDecimal): Json =
        Braid[Json].fromBigDecimal(t)
    }

    implicit case object ShortMagnet extends JsonNumberMagnet[Short] {
      override def apply[Json: Braid](t: Short): Json =
        Braid[Json].fromInt(t.toInt)
    }

    implicit case object ByteMagnet extends JsonNumberMagnet[Byte] {
      override def apply[Json: Braid](t: Byte): Json =
        Braid[Json].fromInt(t.toInt)
    }
  }

}
