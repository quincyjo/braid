val Scala3 = "3.3.1"
val Scala2_13 = "2.13.12"

val scalatestVersion = "3.2.17"
val scalaTest = "org.scalatest" %% "scalatest" % scalatestVersion
val scalaTestFlatSpec =
  "org.scalatest" %% "scalatest-flatspec" % scalatestVersion

val scalameta = "org.scalameta" %% "munit" % "0.7.29"

val circeVersion = "0.14.6"
val circeCore = "io.circe" %% "circe-core" % circeVersion

val playJsonVersion = "3.0.1"
val playJson = "org.playframework" %% "play-json" % playJsonVersion

val json4sVersion = "4.0.7"
val json4sAST = "org.json4s" %% "json4s-ast" % json4sVersion

// skip / publish := true
ThisBuild / tlBaseVersion := "0.1"
ThisBuild / version := "0.1.0"
// Default to same as circe or SBT isn't happy.
// https://github.com/sbt/sbt/issues/3465
ThisBuild / scalaVersion := Scala2_13
ThisBuild / crossScalaVersions := List(Scala2_13, Scala3)
ThisBuild / organization := "com.quincyjo"
ThisBuild / organizationName := "Quincy Jo"
ThisBuild / organizationHomepage := Some(url("https://quincyjo.com"))
ThisBuild / homepage := Some(url("https://github.com/quincyjo/braid"))
ThisBuild / startYear := Some(2024)
ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/quincyjo/braid"),
    "git@github.com:quincyjo/braid.git"
  )
)
ThisBuild / developers := List(
  Developer(
    "quincyjo",
    "Quincy Jo",
    "me@quincyjo.com",
    url("https://github.com/quincyjo")
  )
)
ThisBuild / licenses := Seq(License.Apache2)
ThisBuild / tlJdkRelease := Some(11)

Global / excludeLintKeys += tlBaseVersion

val commonSettings = Seq(
  libraryDependencies ++= Seq(
    scalameta % Test,
    scalaTest % Test,
    scalaTestFlatSpec % Test
  ),
  scalacOptions ++= (
    if (!tlIsScala3.value)
      Seq(
        "-feature",
        "-language:implicitConversions"
      )
    else Seq.empty
  )
)

lazy val root = tlCrossRootProject
  .aggregate(core, operations, circe, play, json4s)

lazy val core = project
  .in(file("modules/core"))
  .settings(
    name := "Braid",
    moduleName := "braid",
    commonSettings
  )

lazy val operations = project
  .in(file("modules/operations"))
  .dependsOn(core, jsonBean % Test)
  .settings(
    name := "Braid Json Operations",
    moduleName := "braid-json-operations",
    commonSettings
  )

lazy val circe = project
  .in(file("modules/circe"))
  .dependsOn(core, testBehaviours % Test)
  .settings(
    name := "Braid Circe",
    moduleName := "braid-circe",
    skip := tlIsScala3.value,
    update / skip := false,
    libraryDependencies ++= (
      if (tlIsScala3.value) Nil
      else
        Seq(
          scalameta,
          scalaTest,
          scalaTestFlatSpec,
          circeCore
        )
    )
  )

lazy val play = project
  .in(file("modules/play"))
  .dependsOn(core, testBehaviours % Test)
  .settings(
    name := "Braid Play",
    moduleName := "braid-play",
    commonSettings,
    libraryDependencies += playJson
  )

lazy val json4s = project
  .in(file("modules/json4s"))
  .dependsOn(core, testBehaviours % Test)
  .settings(
    name := "Braid Json4s",
    moduleName := "braid-json4s",
    commonSettings,
    libraryDependencies += json4sAST
  )

lazy val testBehaviours = project
  .in(file("modules/test-behaviours"))
  .dependsOn(core)
  .settings(
    skip := true,
    publish / skip := true,
    update / skip := false,
    compile / skip := false,
    libraryDependencies ++= Seq(
      scalameta,
      scalaTest,
      scalaTestFlatSpec
    )
  )

lazy val jsonBean = project
  .in(file("modules/json-bean"))
  .dependsOn(core)
  .settings(
    skip := true,
    publish / skip := true,
    update / skip := false,
    compile / skip := false
  )
