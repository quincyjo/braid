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

import org.scalatest.OptionValues
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks

trait JsonNumberSpecLike
    extends AnyFlatSpecLike
    with Matchers
    with TableDrivenPropertyChecks
    with OptionValues {

  def jsonNumberFor[Json](implicit braid: Braid[Json]): Unit = {

    "toBigDecimal" should "return the exact decimal representation of integer values" in {
      val cases = Table(
        "json" -> "expected",
        braid.fromInt(0) -> BigDecimal(0),
        braid.fromInt(42) -> BigDecimal(42),
        braid.fromInt(-42) -> BigDecimal(-42),
        braid.fromLong(Long.MaxValue) -> BigDecimal(Long.MaxValue),
        braid.fromLong(Long.MinValue) -> BigDecimal(Long.MinValue),
        braid.fromBigInt(BigInt("99999999999999999999")) -> BigDecimal(
          BigInt("99999999999999999999")
        )
      )

      forAll(cases) { (json, expected) =>
        braid.asNumber(json).value.toBigDecimal.value should be(expected)
      }
    }

    it should "return the exact decimal value for BigDecimal numbers" in {
      val cases = Table(
        "json" -> "expected",
        braid.fromBigDecimal(BigDecimal("3.14")) -> BigDecimal("3.14"),
        braid.fromBigDecimal(BigDecimal("-99.99")) -> BigDecimal("-99.99"),
        braid.fromBigDecimal(BigDecimal("0.001")) -> BigDecimal("0.001"),
        braid.fromBigDecimal(BigDecimal("1e20")) -> BigDecimal("1e20")
      )

      forAll(cases) { (json, expected) =>
        braid.asNumber(json).value.toBigDecimal.value should be(expected)
      }
    }

    it should "return a decimal representation of finite double values" in {
      val cases = Table(
        "json" -> "expected",
        braid.fromDouble(0.0).value -> BigDecimal(0.0),
        braid.fromDouble(1.5).value -> BigDecimal(1.5),
        braid.fromDouble(-3.14).value -> BigDecimal(-3.14)
      )

      forAll(cases) { (json, expected) =>
        braid.asNumber(json).value.toBigDecimal.value should be(expected)
      }
    }

    "toBigInt" should "return Some for whole number values" in {
      val cases = Table(
        "json" -> "expected",
        braid.fromInt(0) -> BigInt(0),
        braid.fromInt(42) -> BigInt(42),
        braid.fromInt(-42) -> BigInt(-42),
        braid.fromLong(Long.MaxValue) -> BigInt(Long.MaxValue),
        braid.fromLong(Long.MinValue) -> BigInt(Long.MinValue),
        braid.fromBigInt(BigInt("99999999999999999999")) -> BigInt(
          "99999999999999999999"
        ),
        braid.fromBigDecimal(BigDecimal("100.00")) -> BigInt(100)
      )

      forAll(cases) { (json, expected) =>
        braid.asNumber(json).value.toBigInt.value should be(expected)
      }
    }

    it should "return None for fractional values" in {
      val cases = Table(
        "json",
        braid.fromBigDecimal(BigDecimal("3.14")),
        braid.fromBigDecimal(BigDecimal("0.5")),
        braid.fromBigDecimal(BigDecimal("-1.1")),
        braid.fromDouble(1.5).value,
        braid.fromDouble(-0.1).value
      )

      forAll(cases) { json =>
        braid.asNumber(json).value.toBigInt should be(empty)
      }
    }

    "toDouble" should "return the exact double value for small integer values" in {
      val cases = Table(
        "json" -> "expected",
        braid.fromInt(0) -> 0.0,
        braid.fromInt(42) -> 42.0,
        braid.fromInt(-42) -> -42.0,
        braid.fromLong(1000000L) -> 1000000.0
      )

      forAll(cases) { (json, expected) =>
        braid.asNumber(json).value.toDouble should be(expected)
      }
    }

    it should "return PositiveInfinity for values exceeding Double.MaxValue" in {
      val cases = Table(
        "json",
        braid.fromBigDecimal(BigDecimal(Double.MaxValue) * 2),
        braid.fromBigInt(BigInt(2).pow(1074))
      )

      forAll(cases) { json =>
        braid.asNumber(json).value.toDouble should be(Double.PositiveInfinity)
      }
    }

    it should "return NegativeInfinity for values below negative Double.MaxValue" in {
      val cases = Table(
        "json",
        braid.fromBigDecimal(BigDecimal(Double.MaxValue) * -2),
        braid.fromBigInt(-BigInt(2).pow(1074))
      )

      forAll(cases) { json =>
        braid.asNumber(json).value.toDouble should be(Double.NegativeInfinity)
      }
    }

    it should "return a finite approximation for values within Double range despite precision loss" in {
      // 2^53 + 1 cannot be represented exactly as a Double since it exceeds the
      // 53-bit mantissa, but it is still within Double's representable range and
      // should produce a finite result rather than failing or returning NaN.
      val json = braid.fromBigInt(BigInt(2).pow(53) + 1)
      val result = braid.asNumber(json).value.toDouble
      result should not be Double.PositiveInfinity
      result should not be Double.NegativeInfinity
      result.isNaN should be(false)
    }

    "toFloat" should "return the exact float value for small integer values" in {
      val cases = Table(
        "json" -> "expected",
        braid.fromInt(0) -> 0.0f,
        braid.fromInt(42) -> 42.0f,
        braid.fromInt(-42) -> -42.0f
      )

      forAll(cases) { (json, expected) =>
        braid.asNumber(json).value.toFloat should be(expected)
      }
    }

    it should "return PositiveInfinity for values exceeding Float.MaxValue" in {
      val cases = Table(
        "json",
        braid.fromBigDecimal(BigDecimal(Float.MaxValue.toDouble) * 2),
        braid.fromBigInt(BigInt(2).pow(200))
      )

      forAll(cases) { json =>
        braid.asNumber(json).value.toFloat should be(Float.PositiveInfinity)
      }
    }

    it should "return NegativeInfinity for values below negative Float.MaxValue" in {
      val cases = Table(
        "json",
        braid.fromBigDecimal(BigDecimal(Float.MaxValue.toDouble) * -2),
        braid.fromBigInt(-BigInt(2).pow(200))
      )

      forAll(cases) { json =>
        braid.asNumber(json).value.toFloat should be(Float.NegativeInfinity)
      }
    }

    it should "return a finite approximation for values within Float range despite precision loss" in {
      // 2^24 + 1 cannot be represented exactly as a Float since it exceeds the
      // 24-bit mantissa, but it is still within Float's representable range and
      // should produce a finite result rather than failing or returning NaN.
      val json = braid.fromBigInt(BigInt(2).pow(24) + 1)
      val result = braid.asNumber(json).value.toFloat
      result should not be Float.PositiveInfinity
      result should not be Float.NegativeInfinity
      result.isNaN should be(false)
    }

    "toLong" should "return Some for values that are valid Longs" in {
      val cases = Table(
        "json" -> "expected",
        braid.fromInt(0) -> 0L,
        braid.fromInt(42) -> 42L,
        braid.fromInt(-42) -> -42L,
        braid.fromLong(Long.MaxValue) -> Long.MaxValue,
        braid.fromLong(Long.MinValue) -> Long.MinValue,
        braid.fromBigInt(BigInt(Long.MaxValue)) -> Long.MaxValue,
        braid.fromBigDecimal(BigDecimal(Long.MinValue)) -> Long.MinValue
      )

      forAll(cases) { (json, expected) =>
        braid.asNumber(json).value.toLong.value should be(expected)
      }
    }

    it should "return None for fractional values" in {
      val cases = Table(
        "json",
        braid.fromBigDecimal(BigDecimal("3.5")),
        braid.fromBigDecimal(BigDecimal("0.1")),
        braid.fromBigDecimal(BigDecimal("-1.9"))
      )

      forAll(cases) { json =>
        braid.asNumber(json).value.toLong should be(empty)
      }
    }

    it should "return None for values outside the Long range" in {
      val cases = Table(
        "json",
        braid.fromBigInt(BigInt(Long.MaxValue) + 1),
        braid.fromBigInt(BigInt(Long.MinValue) - 1),
        braid.fromBigDecimal(BigDecimal(Long.MaxValue) + 1)
      )

      forAll(cases) { json =>
        braid.asNumber(json).value.toLong should be(empty)
      }
    }

    "toInt" should "return Some for values within Int range" in {
      val cases = Table(
        "json" -> "expected",
        braid.fromInt(0) -> 0,
        braid.fromInt(42) -> 42,
        braid.fromInt(Int.MaxValue) -> Int.MaxValue,
        braid.fromInt(Int.MinValue) -> Int.MinValue
      )

      forAll(cases) { (json, expected) =>
        braid.asNumber(json).value.toInt.value should be(expected)
      }
    }

    it should "return None for values outside the Int range" in {
      val cases = Table(
        "json",
        braid.fromLong(Int.MaxValue.toLong + 1),
        braid.fromLong(Int.MinValue.toLong - 1),
        braid.fromBigInt(BigInt(Long.MaxValue))
      )

      forAll(cases) { json =>
        braid.asNumber(json).value.toInt should be(empty)
      }
    }

    it should "return None for fractional values" in {
      val cases = Table(
        "json",
        braid.fromBigDecimal(BigDecimal("1.5")),
        braid.fromBigDecimal(BigDecimal("-0.5"))
      )

      forAll(cases) { json =>
        braid.asNumber(json).value.toInt should be(empty)
      }
    }

    "toShort" should "return Some for values within Short range" in {
      val cases = Table(
        "json" -> "expected",
        braid.fromInt(0) -> (0: Short),
        braid.fromInt(42) -> (42: Short),
        braid.fromInt(Short.MaxValue) -> Short.MaxValue,
        braid.fromInt(Short.MinValue) -> Short.MinValue
      )

      forAll(cases) { (json, expected) =>
        braid.asNumber(json).value.toShort.value should be(expected)
      }
    }

    it should "return None for values outside the Short range" in {
      val cases = Table(
        "json",
        braid.fromInt(Short.MaxValue.toInt + 1),
        braid.fromInt(Short.MinValue.toInt - 1),
        braid.fromLong(Long.MaxValue)
      )

      forAll(cases) { json =>
        braid.asNumber(json).value.toShort should be(empty)
      }
    }

    it should "return None for fractional values" in {
      val cases = Table(
        "json",
        braid.fromBigDecimal(BigDecimal("1.5")),
        braid.fromBigDecimal(BigDecimal("-0.5"))
      )

      forAll(cases) { json =>
        braid.asNumber(json).value.toShort should be(empty)
      }
    }

    "toByte" should "return Some for values within Byte range" in {
      val cases = Table(
        "json" -> "expected",
        braid.fromInt(0) -> (0: Byte),
        braid.fromInt(42) -> (42: Byte),
        braid.fromInt(Byte.MaxValue) -> Byte.MaxValue,
        braid.fromInt(Byte.MinValue) -> Byte.MinValue
      )

      forAll(cases) { (json, expected) =>
        braid.asNumber(json).value.toByte.value should be(expected)
      }
    }

    it should "return None for values outside the Byte range" in {
      val cases = Table(
        "json",
        braid.fromInt(Byte.MaxValue.toInt + 1),
        braid.fromInt(Byte.MinValue.toInt - 1),
        braid.fromLong(Long.MaxValue)
      )

      forAll(cases) { json =>
        braid.asNumber(json).value.toByte should be(empty)
      }
    }

    it should "return None for fractional values" in {
      val cases = Table(
        "json",
        braid.fromBigDecimal(BigDecimal("1.5")),
        braid.fromBigDecimal(BigDecimal("-0.5"))
      )

      forAll(cases) { json =>
        braid.asNumber(json).value.toByte should be(empty)
      }
    }
  }
}
