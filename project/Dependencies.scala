import sbt.{CrossVersion, compilerPlugin, _}

object Dependencies {

  object Versions {
    val cats = "2.1.1"
    val fs2 = "2.3.0"
    val catsEffect = "2.1.3"
    val http4s = "0.21.4"
    val circe = "0.13.0"
    val refined = "0.9.14"
    val betterMonadicFor = "0.3.1"
    val scalaCheck = "1.14.3"
    val scalaTestPlusScalaCheck = "3.1.0.0-RC2"
    val scalaTest = "3.1.2"
    val kindProjector = "0.11.0"
  }

  object Libraries {
    val cats: Seq[ModuleID] = Seq(
      "org.typelevel" %% "cats-core",
      "org.typelevel" %% "cats-free"
    ).map(_ % Versions.cats)

    val catsEffect: Seq[ModuleID] = Seq(
      "org.typelevel" %% "cats-effect" % Versions.catsEffect
    )

    val fs2: Seq[ModuleID] = Seq(
      "co.fs2" %% "fs2-core" % Versions.fs2
    )

    val refined: Seq[ModuleID] = Seq(
      "eu.timepit" %% "refined",
      "eu.timepit" %% "refined-cats"
    ).map(_ % Versions.refined)

    val circe: Seq[ModuleID] = Seq(
      "io.circe" %% "circe-core",
      "io.circe" %% "circe-parser",
      "io.circe" %% "circe-generic",
      "io.circe" %% "circe-generic-extras",
      "io.circe" %% "circe-refined"
    ).map(_ % Versions.circe)

    val http4s: Seq[ModuleID] = Seq(
      "org.http4s" %% "http4s-dsl",
      "org.http4s" %% "http4s-blaze-server",
      "org.http4s" %% "http4s-blaze-client",
      "org.http4s" %% "http4s-circe",
      "org.http4s" %% "http4s-async-http-client"
    ).map(_ % Versions.http4s)

    val kindProjector: ModuleID = compilerPlugin(
      "org.typelevel" %% "kind-projector" % Versions.kindProjector cross CrossVersion.full
    )

    val betterMonadicFor: ModuleID = compilerPlugin(
      "com.olegpy" %% "better-monadic-for" % Versions.betterMonadicFor
    )

    val scalaCheck: Seq[ModuleID] = Seq(
      "org.scalacheck" %% "scalacheck" % Versions.scalaCheck % Test,
      "eu.timepit" %% "refined-scalacheck" % Versions.refined % Test,
      "org.scalatestplus" %% "scalatestplus-scalacheck" % Versions.scalaTestPlusScalaCheck % Test
    )
    val testLib: Seq[ModuleID] = Seq(
      "org.scalatest" %% "scalatest" % Versions.scalaTest % Test
    )
  }
}
