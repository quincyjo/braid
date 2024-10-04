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
| [Circe JSON](https://github.com/circe/circe)            | :white_check_mark: |        :x:         |
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
scala> val json = implicitly[Braid[JsValue]].fromString("foobar")
val json: play.api.libs.json.JsValue = "foobar"

scala> json.isString
val res0: Boolean = true

scala> json.isNumber
val res1: Boolean = false

scala> json.asString
val res2: Option[String] = Some(foobar)

scala> json.mapString(string => s"$string mutated")
val res3: play.api.libs.json.JsValue = "foobar mutated"

scala> json.mapNumber(_ + 3)
val res4: play.api.libs.json.JsValue = "foobar"
```

## Additional Modules

### Circe Support

Provided Braid for [Circe JSON](https://github.com/circe/circe).

```
libraryDependencies += "com.quincyjo" %% "braid-circe" % -version-
```

### Play Support

Provided Braid for [Play JSON](https://github.com/playframework/play-json).

```
libraryDependencies += "com.quincyjo" %% "braid-play" % -version-
```

### Json4s Support

Provided Braid for [Json4s](https://github.com/json4s/json4s).

```
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
