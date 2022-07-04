import sbt._

object Dependencies {

  object Versions {
    val cats = "2.8.0"
    val catsEffect = "3.3.13"
    val fs2 = "3.2.8"
    val logback = "1.2.11"
    val newtype = "0.4.4"
    val refined = "0.10.0"
    val betterMonadicFor = "0.3.1"
    val kindProjector = "0.13.2"
    val skunk = "0.3.1"
    val http4s = "1.0.0-M33"
    val circe = "0.15.0-M1"
    val http4sJwtAuth = "1.0.0"
    val log4cats = "1.7.0"
    val catsRetry = "3.1.0"
    val redis4cats = "1.2.0"
    val ciris = "2.3.2"
    val pureConfig = "0.17.1"

    val scalaCheck = "1.16.0"
    val scalaTest = "3.2.12"
    val scalaTestPlus = "3.2.2.0"
  }

  object Libraries {
    val cats = "org.typelevel" %% "cats-core" % Versions.cats
    val catsEffect = "org.typelevel" %% "cats-effect" % Versions.catsEffect
    val catsRetry = "com.github.cb372" %% "cats-retry" % Versions.catsRetry

    val fs2 = "co.fs2" %% "fs2-core" % Versions.fs2
    def http4s(artifact: String): ModuleID = "org.http4s" %% s"http4s-$artifact" % Versions.http4s
    def circe(artifact: String): ModuleID = "io.circe" %% artifact % Versions.circe
    def ciris(artifact: String): ModuleID = "is.cir" %% artifact % Versions.ciris

    val redis4catsEffects = "dev.profunktor" %% "redis4cats-effects" % Versions.redis4cats
    val redis4catsLog4cats = "dev.profunktor" %% "redis4cats-log4cats" % Versions.redis4cats

    val pureConfig = "com.github.pureconfig" %% "pureconfig" % Versions.pureConfig

    val circeCore = circe("circe-core")
    val circeGeneric = circe("circe-generic")
    val circeParser = circe("circe-parser")
    val circeRefined = circe("circe-refined")

    val cirisCore = ciris("ciris")
    val cirisEnum = ciris("ciris-enumeratum")
    val cirisRefined = ciris("ciris-refined")

    val http4sDsl = http4s("dsl")
    val http4sServer = http4s("ember-server")
    val http4sClient = http4s("ember-client")
    val http4sCirce = http4s("circe")

    val http4sJwtAuth = "dev.profunktor" %% "http4s-jwt-auth" % Versions.http4sJwtAuth

    val refinedCore = "eu.timepit" %% "refined" % Versions.refined
    val refinedCats = "eu.timepit" %% "refined-cats" % Versions.refined
    val refinedPureconfig = "eu.timepit" %% "refined-pureconfig" % Versions.refined
    val newtype = "io.estatico" %% "newtype" % Versions.newtype

    val skunk = "org.tpolecat" %% "skunk-core" % Versions.skunk
    val skunkCirce = "org.tpolecat" %% "skunk-circe" % Versions.skunk
    // Runtime
    val logback = "ch.qos.logback" % "logback-classic" % Versions.logback

    val scalaCheck = "org.scalacheck" %% "scalacheck" % Versions.scalaCheck % "it,test"
    val scalaTest = "org.scalatest" %% "scalatest" % Versions.scalaTest % "it,test"
    val scalaTestPlus = "org.scalatestplus" %% "scalacheck-1-14" % Versions.scalaTestPlus % "it,test"

  }

  object CompilerPlugins {
    val betterMonadicFor = compilerPlugin("com.olegpy" %% "better-monadic-for" % Versions.betterMonadicFor)
    val kindProjector = compilerPlugin(
      ("org.typelevel" %% "kind-projector" % Versions.kindProjector).cross(CrossVersion.full)
    )
  }

  val rootDependencies = Seq(
//    compilerPlugin(CompilerPlugins.betterMonadicFor),
//    CompilerPlugins.kindProjector,
    //compilerPlugin(("org.scalamacros" % "paradise"  % "2.1.1") cross CrossVersion.full),
    "org.typelevel" %% "squants" % "1.8.3",
    Libraries.cats,
    Libraries.catsRetry,
    Libraries.redis4catsEffects,
    Libraries.redis4catsLog4cats,
    Libraries.http4sDsl,
    Libraries.http4sClient,
    Libraries.http4sServer,
    Libraries.http4sCirce,
    Libraries.http4sJwtAuth.cross(CrossVersion.for3Use2_13).excludeAll(
      ExclusionRule(organization = "org.http4s")
    ),
    Libraries.logback % Runtime,
//    Libraries.pureConfig,
//    Libraries.refinedPureconfig,
    Libraries.circeCore,
    Libraries.circeGeneric,
    Libraries.circeParser,
    Libraries.circeRefined,
    Libraries.catsEffect,
    Libraries.cirisCore,
    Libraries.cirisEnum.cross(CrossVersion.for3Use2_13),
    Libraries.cirisRefined,
    Libraries.fs2.cross(CrossVersion.for3Use2_13),
    Libraries.skunk,
    Libraries.skunkCirce,
    Libraries.newtype.cross(CrossVersion.for3Use2_13),
    Libraries.refinedCore,
    Libraries.refinedCats,
    Libraries.scalaCheck,
    Libraries.scalaTest,
    Libraries.scalaTestPlus.cross(CrossVersion.for3Use2_13)
  )

}
