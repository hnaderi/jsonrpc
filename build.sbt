import sbt.ThisBuild
import Dependencies.Libraries._

name := "json-rpc"

val libVersion = "0.1.0"

version := libVersion

lazy val scala2_13 = "2.13.2"
ThisBuild / scalaVersion := scala2_13
ThisBuild / version := libVersion


val core = Project("json-rpc-core", file("json-rpc"))
  .settings(
  	Common.settings,
    libraryDependencies ++= cats ++ catsEffect ++ http4s ++ fs2 ++ circe ++ refined
  )

val docs = Project("json-rpc-docs", file("docs-build"))
  .dependsOn(core)
  .enablePlugins(MdocPlugin)

mdocVariables := Map(
  "VERSION" -> version.value
)

val root = (project in file("."))
  .settings(
    Common.settings,
    version := libVersion
  )
  .aggregate(core, docs)
  .enablePlugins(MicrositesPlugin)
