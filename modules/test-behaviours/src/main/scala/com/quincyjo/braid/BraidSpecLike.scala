/*
 * Copyright 2023 Quincy Jo
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
import com.quincyjo.braid.numbers.JsonNumber

trait BraidSpecLike
    extends AnyFlatSpecLike
    with Matchers
    with TableDrivenPropertyChecks
    with OptionValues {

  def braidFor[Json](implicit braid: Braid[Json]): Unit = {

    "asObject" should "be None for non-objects" in {
      val cases = Table(
        "json",
        braid.arr(),
        braid.fromValues(Vector.tabulate(5)(braid.fromInt)),
        braid.fromBoolean(true),
        braid.fromBoolean(false),
        braid.fromString("foobar"),
        braid.fromInt(42),
        braid.Null
      )

      forAll(cases) { json =>
        braid.asObject(json) should be(empty)
      }
    }

    it should "return a map of an object's attributes" in {
      val cases = Table(
        "json" -> "expected",
        braid.obj() -> Map.empty[String, Json],
        braid.obj("foobar" -> braid.fromInt(42)) -> Map(
          "foobar" -> braid.fromInt(42)
        )
      )

      forAll(cases) { (json, expected) =>
        braid.asObject(json).value should be(expected)
      }
    }

    "asArray" should "be None for non-arrays" in {
      val cases = Table(
        "json",
        braid.obj(),
        braid.obj("foobar" -> braid.fromInt(42)),
        braid.fromString("foobar"),
        braid.fromInt(42),
        braid.fromBoolean(true),
        braid.Null
      )

      forAll(cases) { json =>
        braid.asArray(json) should be(empty)
      }
    }

    it should "return the values of an array" in {
      val cases = Table(
        "json" -> "expected",
        braid.arr() -> Vector.empty[Json],
        braid.arr(braid.fromInt(1)) -> Vector(braid.fromInt(1)),
        braid
          .arr(braid.fromInt(1), braid.fromInt(2), braid.fromInt(3)) -> Vector(
          braid.fromInt(1),
          braid.fromInt(2),
          braid.fromInt(3)
        )
      )

      forAll(cases) { (json, expected) =>
        braid
          .asArray(json)
          .value should contain theSameElementsAs expected
      }
    }

    "asString" should "be None for non-strings" in {
      val cases = Table(
        "json",
        braid.fromInt(42),
        braid.fromBoolean(true),
        braid.arr(),
        braid.arr(braid.fromInt(1), braid.fromInt(2), braid.fromInt(3)),
        braid.obj(),
        braid.obj("foobar" -> braid.fromInt(42)),
        braid.Null
      )

      forAll(cases) { json =>
        braid.asString(json) should be(empty)
      }
    }

    it should "be fromString value for JSON strings" in {
      val cases = Table(
        "json" -> "expected",
        braid.fromString("foobar") -> "foobar",
        braid.fromString("") -> "",
        braid.fromString("apples and bananas") -> "apples and bananas"
      )

      forAll(cases) { (json, expected) =>
        braid.asString(json).value should be(expected)
      }
    }

    "asBoolean" should "be None for non-booleans" in {
      val cases = Table(
        "json",
        braid.fromInt(42),
        braid.fromString("foobar"),
        braid.arr(),
        braid.arr(braid.fromInt(1), braid.fromInt(2), braid.fromInt(3)),
        braid.obj(),
        braid.obj("foobar" -> braid.fromInt(42)),
        braid.Null
      )

      forAll(cases) { json =>
        braid.asBoolean(json) should be(empty)
      }
    }

    it should "be boolean value for JSON booleans" in {
      val cases = Table(
        "json" -> "expected",
        braid.fromBoolean(true) -> true,
        braid.fromBoolean(false) -> false
      )

      forAll(cases) { (json, expected) =>
        braid.asBoolean(json).value should be(expected)
      }
    }

    "asNumber" should "be None for non-numbers" in {
      val cases = Table(
        "json",
        braid.fromString("foobar"),
        braid.fromBoolean(true),
        braid.arr(),
        braid.arr(braid.fromInt(1), braid.fromInt(2), braid.fromInt(3)),
        braid.obj(),
        braid.obj("foobar" -> braid.fromInt(42)),
        braid.Null
      )

      forAll(cases) { json =>
        braid.asNumber(json) should be(empty)
      }
    }

    it should "be a JsonNumber for JSON numbers" in {
      val cases = Table[Json, JsonNumber[Json] => Any, Any](
        ("json", "accessor", "expected"),
        (braid.fromInt(42), _.toInt, Some(42)),
        (braid.fromInt(0), _.toInt, Some(0)),
        (braid.fromInt(-1), _.toInt, Some(-1)),
        (braid.fromInt(Int.MaxValue), _.toInt, Some(Int.MaxValue)),
        (braid.fromInt(Int.MinValue), _.toInt, Some(Int.MinValue)),
        (braid.fromLong(Long.MaxValue), _.toLong, Some(Long.MaxValue)),
        (braid.fromLong(Long.MinValue), _.toLong, Some(Long.MinValue)),
        (
          braid.fromBigInt(BigInt("99999999999999999999")),
          _.toBigInt,
          Some(BigInt("99999999999999999999"))
        ),
        (
          braid.fromBigDecimal(BigDecimal("3.14")),
          _.toBigDecimal,
          Some(BigDecimal("3.14"))
        ),
        (
          braid.fromBigDecimal(BigDecimal("-0.001")),
          _.toBigDecimal,
          Some(BigDecimal("-0.001"))
        )
      )

      forAll(cases) { (json, accessor, expected) =>
        accessor(braid.asNumber(json).value) should be(expected)
      }
    }

    "asNull" should "be None for non-nulls" in {
      val cases = Table(
        "json",
        braid.fromString("foobar"),
        braid.fromBoolean(true),
        braid.fromInt(42),
        braid.arr(),
        braid.arr(braid.fromInt(1), braid.fromInt(2), braid.fromInt(3)),
        braid.obj(),
        braid.obj("foobar" -> braid.fromInt(42))
      )

      forAll(cases) { json =>
        braid.asNull(json) should be(empty)
      }
    }

    it should "be Unit for JSON nulls" in {
      braid.asNull(braid.Null).value should be(())
    }

    "isObject" should "be false for non-objects" in {
      val cases = Table(
        "json",
        braid.arr(),
        braid.fromValues(Vector.tabulate(5)(braid.fromInt)),
        braid.fromBoolean(true),
        braid.fromBoolean(false),
        braid.fromString("foobar"),
        braid.fromInt(42),
        braid.Null
      )

      forAll(cases) { json =>
        braid.isObject(json) should be(false)
      }
    }

    it should "be true for JSON objects" in {
      val cases = Table(
        "json",
        braid.obj(),
        braid.obj("foobar" -> braid.fromInt(42))
      )

      forAll(cases) { json =>
        braid.isObject(json) should be(true)
      }
    }

    "isArray" should "be false for non-arrays" in {
      val cases = Table(
        "json",
        braid.obj(),
        braid.obj("foobar" -> braid.fromInt(42)),
        braid.fromString("foobar"),
        braid.fromInt(42),
        braid.fromBoolean(true),
        braid.Null
      )

      forAll(cases) { json =>
        braid.isArray(json) should be(false)
      }
    }

    it should "be true for JSON arrays" in {
      val cases = Table(
        "json",
        braid.arr(),
        braid.arr(braid.fromInt(1)),
        braid.arr(braid.fromInt(1), braid.fromInt(2), braid.fromInt(3))
      )

      forAll(cases) { json =>
        braid.isArray(json) should be(true)
      }
    }

    "isString" should "be false for non-strings" in {
      val cases = Table(
        "json",
        braid.fromInt(42),
        braid.fromBoolean(true),
        braid.arr(),
        braid.arr(braid.fromInt(1), braid.fromInt(2), braid.fromInt(3)),
        braid.obj(),
        braid.obj("foobar" -> braid.fromInt(42)),
        braid.Null
      )

      forAll(cases) { json =>
        braid.isString(json) should be(false)
      }
    }

    it should "be true for JSON strings" in {
      val cases = Table(
        "json",
        braid.fromString("foobar"),
        braid.fromString(""),
        braid.fromString("apples and bananas")
      )

      forAll(cases) { json =>
        braid.isString(json) should be(true)
      }
    }

    "isBoolean" should "be false for non-booleans" in {
      val cases = Table(
        "json",
        braid.fromInt(42),
        braid.fromString("foobar"),
        braid.arr(),
        braid.arr(braid.fromInt(1), braid.fromInt(2), braid.fromInt(3)),
        braid.obj(),
        braid.obj("foobar" -> braid.fromInt(42)),
        braid.Null
      )

      forAll(cases) { json =>
        braid.isBoolean(json) should be(false)
      }
    }

    it should "be true for JSON booleans" in {
      val cases = Table(
        "json",
        braid.fromBoolean(true),
        braid.fromBoolean(false)
      )

      forAll(cases) { json =>
        braid.isBoolean(json) should be(true)
      }
    }

    "isNumber" should "be false for non-numbers" in {
      val cases = Table(
        "json",
        braid.fromString("foobar"),
        braid.fromBoolean(true),
        braid.arr(),
        braid.arr(braid.fromInt(1), braid.fromInt(2), braid.fromInt(3)),
        braid.obj(),
        braid.obj("foobar" -> braid.fromInt(42)),
        braid.Null
      )

      forAll(cases) { json =>
        braid.isNumber(json) should be(false)
      }
    }

    it should "be true for JSON numbers" in {
      val cases = Table(
        "json",
        braid.fromInt(42),
        braid.fromInt(0)
      )

      forAll(cases) { json =>
        braid.isNumber(json) should be(true)
      }
    }

    "isNull" should "be false for non-nulls" in {
      val cases = Table(
        "json",
        braid.fromString("foobar"),
        braid.fromBoolean(true),
        braid.fromInt(42),
        braid.arr(),
        braid.arr(braid.fromInt(1), braid.fromInt(2), braid.fromInt(3)),
        braid.obj(),
        braid.obj("foobar" -> braid.fromInt(42))
      )

      forAll(cases) { json =>
        braid.isNull(json) should be(false)
      }
    }

    it should "be true for JSON null" in {
      braid.isNull(braid.Null) should be(true)
    }
  }
}
