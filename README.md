# Braid

Currently there are several mainstream JSON libraries for Scala. While many of these libraries have similar ASTs and
APIs, they are distinct and are inherently different types. This means that when a library is written to add
functionality which relies on JSON data, it is more often then not written against a specific JSON library's
implementation. This prevents such libraries from being used in applications where a different JSON library is used.

Braid is a normalized API for implementing Scala code against JSON independently of the underlying JSON library. This
allows libraries that require a JSON implementation to be implemented generically and automatically gain support for any
JSON library which is part of the braid. Braid currently provides support for Circe JSON, Play JSON, and Json4s
natively, but other JSON libraries may be added in the future, and local braid implementations may be provided as well.

Supported JSON Libraries:

| Library                                                 |     Scala 2.13     |     Scala 3.X      |
|---------------------------------------------------------|:------------------:|:------------------:|
| [Circe JSON](https://github.com/circe/circe)            | :white_check_mark: | :white_check_mark: |
| [Play JSON](https://github.com/playframework/play-json) | :white_check_mark: | :white_check_mark: |
| [Json4s](https://github.com/json4s/json4s)              | :white_check_mark: | :white_check_mark: |

## Getting Started

To get started, you can add braid as a dependency in your project:

* sbt
  ```scala
  libraryDependencies += "com.quincyjo" %% "braid" % -version-
  ```
* Gradle
  ```
  compile group: 'com.quincyjo', name: 'braid_2.13', version: -version-
  ```
* Maven
  ```xml
  <dependency>
    <groupId>com.quincyjo</groupId>
    <artifactId>braid_2.13</artifactId>
    <version>-version-</version>
  </dependency>
  ```

See [GitHub releases](https://github.com/quincyjo/braid/releases) for the correct version.

## Usage

Example usage of creating, accessing, and mutating JSON generically. All functions used are provided by Braid, while the
underlying JSON type is a Play `JsValue`.

```scala
scala > val json = Braid[JsValue].fromString("foobar")
val json: play.api.libs.json.JsValue = "foobar"

scala > json.isString
val res0: Boolean = true

scala > json.isNumber
val res1: Boolean = false

scala > json.asString
val res2: Option[String] = Some(foobar)

scala > json.mapString(string => s"$string mutated")
val res3: play.api.libs.json.JsValue = "foobar mutated"

scala > json.mapNumber(_ + 3)
val res4: play.api.libs.json.JsValue = "foobar"
```

Write functions generically for any JSON type.

```scala
def getValues[Json: Braid](json: Json): Iterable[Json] =
    Braid[Json].arrayOrObject(json)(
        Iterable.single,
        identity,
        _.values   
    )
```

Or with implicit syntax in scope:

```scala
import com.quincyjo.braid.implicits._

def getValues[Json: Braid](json: Json): Iterable[Json] =
    json.arrayOrObject(
        Iterable.single,
        identity,
        _.values   
    )
```

### Numbers

Different JSON libraries store numbers with different internal representations — some distinguish between integers, longs,
doubles, and arbitrary-precision decimals, while others use a single `BigDecimal` for all numbers. Braid normalises this
through `JsonNumber[Json]`, a library-independent wrapper with a consistent API for extracting numeric values in common
Scala types.

`asNumber` returns a `JsonNumber` if the JSON value is a number, `None` otherwise. The `JsonNumber` can then be
interrogated in the desired type. Conversions that may lose information due to range or fractional values return
`Option`, while `toDouble` and `toFloat` always succeed, saturating to `±Infinity` for values outside their
representable range.

This allows the underlying JSON library to maintain its own internal representation of numbers, preserving their support
for precision or larger numbers, such as `Circe`'s `BiggerDecimal`.

```scala
scala> val json = Braid[JsValue].fromBigDecimal(BigDecimal("3.99"))
val json: play.api.libs.json.JsValue = 3.99

scala> val n = Braid[JsValue].asNumber(json).get
val n: JsonNumber[JsValue] = PlayJsonNumber(3.99)

scala> n.toBigDecimal
val res0: Option[BigDecimal] = Some(3.99)

scala> n.toLong
val res1: Option[Long] = None

scala> n.toDouble
val res2: Double = 3.99
```

`mapNumber` applies a function over the `JsonNumber` if the JSON is a number and converts the result back to a JSON
number, leaving the value unchanged if the JSON is not a number. The return type of the function must be one of the
supported numeric types: `Int`, `Long`, `Float`, `Double`, `BigInt`, `BigDecimal`, `Short`, or `Byte`, and is automatically
converted back to an appropriate concrete JSON number for the underlying JSON library.

```scala
scala> val json = Braid[JsValue].fromLong(100L)
val json: play.api.libs.json.JsValue = 100

scala> Braid[JsValue].mapNumber(json)(_.toLong.getOrElse(0L) * 2)
val res3: play.api.libs.json.JsValue = 200

scala> Braid[JsValue].mapNumber(Braid[JsValue].fromString("hello"))(_.toLong.getOrElse(0L) * 2)
val res4: play.api.libs.json.JsValue = "hello"
```

A instance of `JsonNumber` may be narrowed back to the underlying JSON instance if desired. This provides a normalized
API for resaturating a JSON when the underlying library has a distinct class definition for describing numbers from the
JSON AST (eg, Circe and Json4s).

```scala
scala> Braid[JsValue].fromInt(42).asNumber.get.asJson
val res7: play.api.libs.json.JsValue = 42
```

## Additional Modules

### Circe Support

Provided Braid for [Circe JSON](https://github.com/circe/circe).

```sbt
libraryDependencies += "com.quincyjo" %% "braid-circe" % -version-
```

### Play Support

Provided Braid for [Play JSON](https://github.com/playframework/play-json).

```sbt
libraryDependencies += "com.quincyjo" %% "braid-play" % -version-
```

### Json4s Support

Provided Braid for [Json4s](https://github.com/json4s/json4s).

```sbt
libraryDependencies += "com.quincyjo" %% "braid-json4s" % -version-
```

### JSON Operations

The `braid-json-operations` module provides support for JSON operations, meaning performing Javascript style operators
on JSON values; EG, `"1" + 1`. Type coercion is also supported, and operators here follow ES6 coercion rules. This is
meant for libraries which need to support JSON operations for evaluation within Scala. Examples include JSON logic and
JSON path.

The JSON operations module may be added to a project with the following dependency:

```scala
libraryDependencies += "com.quincyjo" %% "braid-json-operations" % -version-
```
