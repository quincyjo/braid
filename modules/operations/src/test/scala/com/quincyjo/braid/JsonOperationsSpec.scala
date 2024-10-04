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

import com.quincyjo.braid.bean.JsonBean
import com.quincyjo.braid.bean.JsonBean._
import com.quincyjo.braid.operations.JsonOperations
import org.scalatest.OptionValues
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers
import org.scalatest.prop.{TableDrivenPropertyChecks, TableFor2, TableFor3}

class JsonOperationsSpec
    extends AnyFlatSpecLike
    with Matchers
    with OptionValues
    with TableDrivenPropertyChecks {

  "coerceToNumber" should "follow JS conversion rules" in {
    val cases: TableFor2[JsonBean, Option[Int]] = Table(
      "json" -> "expected",
      JsonBean.number(42) -> Some(42),
      JsonBean.Null -> Some(0),
      JsonBean.True -> Some(1),
      JsonBean.False -> Some(0),
      JsonBean.string("42") -> Some(42),
      JsonBean.string("") -> Some(0),
      JsonBean.string(" ") -> Some(0),
      JsonBean.string("\n") -> Some(0),
      JsonBean.string("foobar") -> None,
      JsonBean.arr() -> Some(0),
      JsonBean.arr(JsonBean.number(1)) -> Some(1),
      JsonBean.fromValues(Vector.tabulate(5)(JsonBean.number)) -> None,
      JsonBean.obj() -> None,
      JsonBean.obj("foobar" -> 42) -> None
    )

    forAll(cases) { (json, expected) =>
      JsonOperations.coerceToNumber(json) should be(expected)
    }
  }

  "coerceToString" should "follow JS conversion rules" in {
    val cases: TableFor2[JsonBean, String] = Table(
      "json" -> "expected",
      JsonBean.Null -> "null",
      JsonBean.True -> "true",
      JsonBean.False -> "false",
      JsonBean.number(42) -> "42",
      JsonBean.string("foobar") -> "foobar",
      JsonBean.arr() -> "",
      JsonBean.arr("a", 1) -> "a,1",
      JsonBean.obj() -> "[object Object]",
      JsonBean.obj("foobar" -> 42) -> "[object Object]"
    )

    forAll(cases) { (json, expected) =>
      JsonOperations.coerceToString(json) should be(expected)
    }
  }

  "coerceToBoolean" should "follow JS conversion rules" in {
    val cases: TableFor2[JsonBean, Boolean] = Table(
      "json" -> "expected",
      JsonBean.Null -> false,
      JsonBean.True -> true,
      JsonBean.False -> false,
      JsonBean.number(42) -> true,
      JsonBean.number(0) -> false,
      JsonBean.number(-1) -> true,
      JsonBean.string("foobar") -> true,
      JsonBean.string("") -> false,
      JsonBean.arr() -> true,
      JsonBean.arr("a", 1) -> true,
      JsonBean.obj() -> true,
      JsonBean.obj("foobar" -> 42) -> true
    )

    forAll(cases) { (json, expected) =>
      JsonOperations.coerceToBoolean(json) should be(expected)
    }
  }

  "areSameType" should "follow JS conversion rules" in {
    val cases: TableFor3[JsonBean, JsonBean, Boolean] = Table(
      ("left", "right", "expected"),
      (JsonBean.Null, JsonBean.Null, true),
      (JsonBean.True, JsonBean.False, true),
      (JsonBean.string("foobar"), JsonBean.string("deadbeef"), true),
      (JsonBean.number(42), JsonBean.number(42), true),
      (JsonBean.arr(), JsonBean.arr(), true),
      (JsonBean.obj(), JsonBean.obj(), true),
      (JsonBean.arr(), JsonBean.obj(), true),
      (JsonBean.Null, JsonBean.string("foobar"), false),
      (JsonBean.Null, JsonBean.number(42), false),
      (JsonBean.string(""), JsonBean.False, false)
    )

    forAll(cases) { (left, right, expected) =>
      JsonOperations.areSameType(left, right) should be(expected)
    }
  }

  "convertTypes" should "follow JS comparison conversion" in {
    val cases: TableFor3[JsonBean, JsonBean, Option[(JsonBean, JsonBean)]] =
      Table(
        ("a", "b", "expected"),
        (JsonBean.Null, JsonBean.Null, Some(JsonBean.Null -> JsonBean.Null)),
        (JsonBean.True, JsonBean.False, Some(JsonBean.True -> JsonBean.False)),
        (
          JsonBean.string("foobar"),
          JsonBean.string("deadbeef"),
          Some(JsonBean.string("foobar") -> JsonBean.string("deadbeef"))
        ),
        (
          JsonBean.number(42),
          JsonBean.number(42),
          Some(JsonBean.number(42) -> JsonBean.number(42))
        ),
        (
          JsonBean.False,
          JsonBean.string("5"),
          Some(JsonBean.number(0) -> JsonBean.number(5))
        ),
        (
          JsonBean.True,
          JsonBean.string(""),
          Some(JsonBean.number(1) -> JsonBean.number(0))
        ),
        (
          JsonBean.arr(),
          JsonBean.False,
          Some(JsonBean.number(0) -> JsonBean.number(0))
        ),
        (JsonBean.obj(), JsonBean.True, None),
        (JsonBean.arr("foobar"), JsonBean.True, None),
        (JsonBean.Null, JsonBean.False, Some(JsonBean.Null -> JsonBean.False))
      )

    forAll(cases) { case (a, b, expected) =>
      JsonOperations.convertTypes(a, b) should be(expected)
    }
  }

  /*
  "Not" should "be equivalent to JS falsey" in {
    val cases = Table(
      "json" -> "expected",
      JsonBean.boolean(true) -> false,
      JsonBean.boolean(false) -> true,
      JsonBean.fromBigDecimal(0) -> true,
      JsonBean.fromBigDecimal(42) -> false,
      JsonBean.fromBigDecimal(-1) -> false,
      JsonBean.fromString("") -> true,
      JsonBean.fromString("foo") -> false,
      JsonBean.obj() -> false,
      JsonBean.obj("foo" -> "bar") -> false,
      JsonBean.arr() -> false,
      JsonBean.arr(5) -> false
    )

    forAll(cases) { case (json, expected) =>
      Not(JsonPathValue(JsonPath.$))(evaluator, json, json) should be(
        JsonBean.boolean(expected)
      )
    }
  }
   */

  "equal" should "compare same types" in {
    val cases: TableFor3[JsonBean, JsonBean, Boolean] = Table(
      ("left", "right", "expected"),
      (JNumber(42), JNumber(42), true),
      (JNumber(42), JNumber(5), false),
      (JNull, JNull, true),
      (JBoolean(true), JBoolean(true), true),
      (JBoolean(true), JBoolean(false), false),
      (JString("foobar"), JString("foobar"), true),
      (JString("foobar"), JString("deadbeef"), false)
    )

    forAll(cases) { case (left, right, expected) =>
      JsonOperations.equal(left, right) should be(
        JsonBean.boolean(expected)
      )
    }
  }

  it should "apply type conversion" in {
    val cases: TableFor3[JsonBean, JsonBean, Boolean] = Table(
      ("left", "right", "expected"),
      (JNumber(42), JNumber(5), false),
      (JNumber(42), JNumber(42), true),
      (JBoolean(true), JNumber(0), false),
      (JBoolean(false), JNumber(0), true),
      (JNull, JString(""), false),
      (JNull, JNull, true),
      (JString("5"), JNumber(5), true)
    )

    forAll(cases) { case (left, right, expected) =>
      JsonOperations.equal(left, right) should be(
        JsonBean.boolean(expected)
      )
    }
  }

  "GreaterThan" should behave like comparator(JsonOperations.greaterThan)(_ > _)

  "GreaterThanOrEqual" should behave like comparator(
    JsonOperations.greaterThanOrEqualTo
  )(_ >= _)

  "LessThan" should behave like comparator(JsonOperations.lessThan)(_ < _)

  "lessThanOrEqual" should behave like comparator(
    JsonOperations.lessThanOrEqualTo
  )(
    _ <= _
  )

  "Plus" should "add two numbers" in {
    val cases = Table[BigDecimal, BigDecimal](
      ("left", "right"),
      (BigDecimal(42), BigDecimal(5)),
      (BigDecimal(42), BigDecimal(-5)),
      (BigDecimal(42), BigDecimal(0)),
      (BigDecimal(0), BigDecimal(42)),
      (BigDecimal(0), BigDecimal(0)),
      (BigDecimal(0), BigDecimal(-42))
    )

    forAll(cases) { case (left, right) =>
      JsonOperations.plus(
        JsonBean.number(left),
        JsonBean.number(right)
      ) should be(
        JsonBean.JNumber(left + right)
      )
    }
  }

  it should "coerce null to 0" in {
    val cases = Table[BigDecimal](
      "fromBigDecimal",
      BigDecimal(42),
      BigDecimal(0),
      BigDecimal(-42)
    )

    forAll(cases) { number =>
      JsonOperations.plus(JsonBean.number(number), JsonBean.Null) should be(
        JsonBean.JNumber(number)
      )
    }
  }

  it should "concat two strings" in {
    val cases = Table[String, String](
      ("left", "right"),
      ("foo", "bar"),
      ("", "bar"),
      ("foo", ""),
      ("", "")
    )

    forAll(cases) { case (left, right) =>
      JsonOperations.plus(
        JsonBean.string(left),
        JsonBean.string(right)
      ) should be(JsonBean.string(left concat right))
    }
  }

  it should "coerce values to strings if both aren't numbers or null" in {
    val cases = Table[JsonBean, JsonBean](
      ("left", "right"),
      (JsonBean.number(42), JsonBean.string("foobar")),
      (JsonBean.string("foobar"), JsonBean.number(42)),
      (JsonBean.number(5), JsonBean.arr(JsonBean.True)),
      (JsonBean.arr(JsonBean.True), JsonBean.number(5)),
      (JsonBean.obj(), JsonBean.number(0))
    )

    forAll(cases) { case (left, right) =>
      JsonOperations.plus(left, right) should be(
        JsonBean.string(
          JsonOperations.coerceToString(left) concat
            JsonOperations.coerceToString(right)
        )
      )
    }
  }

  "Minus" should behave like arithmeticOperator(JsonOperations.minus)(_ - _)

  "Multiply" should behave like arithmeticOperator(JsonOperations.multiply)(
    _ * _
  )

  "Divide" should behave like arithmeticOperator(JsonOperations.divide)(_ / _)

  def comparator(
      g: (JsonBean, JsonBean) => JsonBean
  )(f: (Int, Int) => Boolean): Unit = {

    it should "compare numbers" in {
      val cases = Table(
        ("left", "right"),
        (42, 5),
        (5, 42),
        (42, 42)
      )

      forAll(cases) { case (a, b) =>
        g(JNumber(a), JNumber(b)) should be(
          JsonBean.boolean(f(a.compareTo(b), 0))
        )
      }
    }

    it should "compare strings alphabetically" in {
      val cases = Table(
        ("left", "right"),
        ("a", "b"),
        ("b", "a"),
        ("a", "a"),
        ("A", "a")
      )

      forAll(cases) { case (a, b) =>
        g(JString(a), JString(b)) should be(
          JsonBean.boolean(f(a.compareTo(b), 0))
        )
      }
    }

    it should "compare arrays by strings" in {
      val cases = Table(
        ("left", "right"),
        (JsonBean.arr(), JsonBean.arr()),
        (JsonBean.arr(1), JsonBean.arr()),
        (JsonBean.arr(), JsonBean.arr(1)),
        (JsonBean.arr(1, 2), JsonBean.arr(3, 4)),
        (JsonBean.arr("a"), JsonBean.arr("b")),
        (JsonBean.arr("a"), JsonBean.arr()),
        (JsonBean.arr("a", "b"), JsonBean.arr("c", "d"))
      )

      forAll(cases) { case (left, right) =>
        g(left, right) should be(
          JsonBean.boolean(
            f(
              JsonOperations
                .coerceToString(left)
                .compareTo(JsonOperations.coerceToString(right)),
              0
            )
          )
        )
      }
    }

    it should "compare arrays to strings as strings" in {
      val cases = Table(
        ("left", "right"),
        (JsonBean.arr(), ""),
        (JsonBean.arr(1), ""),
        (JsonBean.arr(), "1"),
        (JsonBean.arr(1, 2), "3,4"),
        (JsonBean.arr("a"), "b"),
        (JsonBean.arr("a"), ""),
        (JsonBean.arr("a", "b"), "c,d")
      )

      forAll(cases) { case (left, right) =>
        g(left, JsonBean.string(right)) should be(
          JsonBean.boolean(
            f(
              JsonOperations
                .coerceToString(left)
                .compareTo(right),
              0
            )
          )
        )
        g(JsonBean.string(right), left) should be(
          JsonBean.boolean(
            f(
              right.compareTo(
                JsonOperations
                  .coerceToString(left)
              ),
              0
            )
          )
        )
      }
    }

    it should "be false if either right is NaN" in {
      val cases = Table[JsonBean, JsonBean](
        ("left", "right"),
        (JsonBean.arr("foobar"), JsonBean.number(0)),
        (JsonBean.arr(1, 2, 3), JsonBean.number(0)),
        (JsonBean.number(1), JsonBean.obj()),
        (JsonBean.number(42), JsonBean.string("foobar")),
        (JsonBean.arr("a"), JsonBean.Null)
      )

      forAll(cases) { case (left, right) =>
        g(left, right) should be(JsonBean.boolean(false))
      }
    }
  }

  def arithmeticOperator(
      g: (JsonBean, JsonBean) => JsonBean
  )(f: (BigDecimal, BigDecimal) => BigDecimal): Unit = {

    it should "operate on two numbers" in {
      val cases = Table[BigDecimal, BigDecimal](
        ("left", "right"),
        (1, 2),
        (-1, 2),
        (2, -1),
        (1, 1),
        (-1, -1),
        (1.1, 2.2),
        (4.4, 3.3)
      )

      forAll(cases) { case (left, right) =>
        g(JNumber(left), JNumber(right)) should be(
          JsonBean.JNumber(f(left, right))
        )
      }
    }

    it should "coerce values into numbers" in {
      val cases = Table[JsonBean, JsonBean](
        ("left", "right"),
        (JsonBean.Null, JsonBean.number(1)),
        (JsonBean.boolean(true), JsonBean.number(1)),
        (JsonBean.boolean(false), JsonBean.number(1)),
        (JsonBean.string(""), JsonBean.number(1)),
        (JsonBean.string("1"), JsonBean.number(1)),
        (JsonBean.arr(), JsonBean.number(1)),
        (JsonBean.arr(JsonBean.number(42)), JsonBean.number(1))
      )

      forAll(cases) { case (left, right) =>
        val coercedLeft = JsonOperations.coerceToNumber(left).value
        val coercedRight = JsonOperations.coerceToNumber(right).value
        g(left, right) should be(JsonBean.JNumber(f(coercedLeft, coercedRight)))
      }
    }

    it should "be null for NaN operands" in {
      val cases = Table(
        ("left", "right"),
        (JsonBean.string("foobar"), JsonBean.number(0)),
        (
          JsonBean.JArray(Vector.tabulate(3)((JsonBean.JNumber(_)))),
          JsonBean.number(0)
        ),
        (JsonBean.arr(JsonBean.string("foobar")), JsonBean.number(0)),
        (JsonBean.obj(), JsonBean.number(0))
      )

      forAll(cases) { case (left, right) =>
        g(left, right) should be(JsonBean.Null)
      }
    }
  }
}
