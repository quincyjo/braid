import scala.scalanative.build._
import sbtcrossproject.CrossPlugin.autoImport._

val Scala3 = "3.3.6"
val Scala2_13 = "2.13.17"

val scalatestVersion = "3.2.19"
val scalametaVersion = "1.0.4"
val circeVersion = "0.14.14"
val playJsonVersion = "3.1.0-M9"
val json4sVersion = "4.1.0-M8"

// skip / publish := true
ThisBuild / tlBaseVersion := "0.1"
ThisBuild / version := "0.1.1"
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
ThisBuild / tlJdkRelease := Some(17)

ThisBuild / dependencyOverrides += "org.typelevel" %% "kind-projector" % "0.13.4"
ThisBuild / evictionErrorLevel := Level.Warn


Global / excludeLintKeys += tlBaseVersion

// Increase heap size for Scala Native linking
fork := true
javaOptions += "-Xmx4G"

val commonSettings = Seq(
  libraryDependencies ++= Seq(
    "org.scalameta" %%% "munit" % scalametaVersion % Test,
    "org.scalatest" %%% "scalatest" % scalatestVersion % Test,
    "org.scalatest" %%% "scalatest-flatspec" % scalatestVersion % Test
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

val nativeSettings = Seq(
  nativeConfig ~= {
    _.withLTO(LTO.full)
      .withMode(Mode.releaseFast)
      .withGC(GC.commix)
  }
)

lazy val root = tlCrossRootProject
  .aggregate(core, operations, circe, play, json4s)

lazy val core = crossProject(JSPlatform, JVMPlatform, NativePlatform)
  .crossType(CrossType.Pure)
  .in(file("modules/core"))
  .settings(
    name := "Braid",
    moduleName := "braid",
    commonSettings
  )
  .nativeSettings(nativeSettings)

lazy val operations = crossProject(JSPlatform, JVMPlatform, NativePlatform)
  .crossType(CrossType.Pure)
  .in(file("modules/operations"))
  .dependsOn(core, jsonBean % Test)
  .settings(
    name := "Braid Json Operations",
    moduleName := "braid-json-operations",
    commonSettings
  )
  .nativeSettings(nativeSettings)

lazy val circe = crossProject(JSPlatform, JVMPlatform, NativePlatform)
  .crossType(CrossType.Pure)
  .in(file("modules/circe"))
  .dependsOn(core, testBehaviours % Test)
  .settings(
    name := "Braid Circe",
    moduleName := "braid-circe",
    libraryDependencies += "io.circe" %%% "circe-core" % circeVersion,
    tlVersionIntroduced := Map("3" -> "0.1.1")
  )
  .nativeSettings(nativeSettings)

lazy val play = crossProject(JSPlatform, JVMPlatform, NativePlatform)
  .crossType(CrossType.Pure)
  .in(file("modules/play"))
  .dependsOn(core, testBehaviours % Test)
  .settings(
    name := "Braid Play",
    moduleName := "braid-play",
    commonSettings,
    libraryDependencies += "org.playframework" %%% "play-json" % playJsonVersion
  )
  .nativeSettings(nativeSettings)

lazy val json4s = crossProject(JSPlatform, JVMPlatform, NativePlatform)
  .crossType(CrossType.Pure)
  .in(file("modules/json4s"))
  .dependsOn(core, testBehaviours % Test)
  .settings(
    name := "Braid Json4s",
    moduleName := "braid-json4s",
    commonSettings,
    libraryDependencies += "org.json4s" %%% "json4s-ast" % json4sVersion
  )
  .nativeSettings(nativeSettings)

lazy val testBehaviours = crossProject(JSPlatform, JVMPlatform, NativePlatform)
  .crossType(CrossType.Pure)
  .in(file("modules/test-behaviours"))
  .dependsOn(core)
  .settings(
    skip := true,
    publish / skip := true,
    update / skip := false,
    compile / skip := false,
    libraryDependencies ++= Seq(
      "org.scalameta" %%% "munit" % scalametaVersion,
      "org.scalatest" %%% "scalatest" % scalatestVersion,
      "org.scalatest" %%% "scalatest-flatspec" % scalatestVersion
    )
  )
  .nativeSettings(nativeSettings)

lazy val jsonBean = crossProject(JSPlatform, JVMPlatform, NativePlatform)
  .crossType(CrossType.Pure)
  .in(file("modules/json-bean"))
  .dependsOn(core)
  .settings(
    skip := true,
    publish / skip := true,
    update / skip := false,
    compile / skip := false
  )
  .nativeSettings(nativeSettings)
