import sbt._

object Dependencies {

  object Versions {
    val cats = "2.4.2"
    val catsEffect = "2.3.3"
    val catsMeowMtl = "0.4.1"
    val derevo = "0.12.1"
    val fs2 = "2.5.3"
    val logback = "1.2.3"
    val newtype = "0.4.4"
    val refined = "0.9.21"

    val betterMonadicFor = "0.3.1"
    val kindProjector = "0.11.3"
    val skunk = "0.0.24"
    val http4s = "0.21.20"
    val circe = "0.13.0"
    val http4sJwtAuth = "0.0.5"
    val log4cats = "1.2.0"
    val catsRetry = "2.1.0"
    val redis4cats = "0.12.0"
    val ciris = "1.2.1"
    val pureConfig = "0.14.1"
    val weaver = "0.6.0-M6"
  }

  object Libraries {
    val cats = "org.typelevel" %% "cats-core" % Versions.cats
    val catsEffect = "org.typelevel" %% "cats-effect" % Versions.catsEffect
    val catsRetry = "com.github.cb372" %% "cats-retry" % Versions.catsRetry

    val fs2 = "co.fs2" %% "fs2-core" % Versions.fs2
    def http4s(artifact: String): ModuleID = "org.http4s" %% s"http4s-$artifact" % Versions.http4s
    def circe(artifact: String): ModuleID = "io.circe" %% artifact % Versions.circe
    def ciris(artifact: String): ModuleID = "is.cir" %% artifact % Versions.ciris
    def derevo(artifact: String): ModuleID = "tf.tofu"    %% s"derevo-$artifact" % Versions.derevo

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

    val catsMeowMtlCore = "com.olegpy" %% "meow-mtl-core" % Versions.catsMeowMtl
    val catsMeowMtlEffects = "com.olegpy" %% "meow-mtl-effects" % Versions.catsMeowMtl

    val derevoCore  = derevo("core")
    val derevoCats  = derevo("cats")
    val derevoCirce = derevo("circe-magnolia")

    val refinedCore = "eu.timepit" %% "refined" % Versions.refined
    val refinedCats = "eu.timepit" %% "refined-cats" % Versions.refined
    val refinedPureconfig = "eu.timepit" %% "refined-pureconfig" % Versions.refined

    val newtype = "io.estatico" %% "newtype" % Versions.newtype

    val skunkCore = "org.tpolecat" %% "skunk-core" % Versions.skunk
    val skunkCirce = "org.tpolecat" %% "skunk-circe" % Versions.skunk
    // Runtime
    val logback = "ch.qos.logback" % "logback-classic" % Versions.logback
    val log4cats = "org.typelevel" %% "log4cats-slf4j" % Versions.log4cats

    // Test
    val weaverCats = "com.disneystreaming" %% "weaver-cats" % Versions.weaver
    val weaverScalaCheck = "com.disneystreaming" %% "weaver-scalacheck" % Versions.weaver

  }

  object CompilerPlugins {
    val betterMonadicFor = compilerPlugin("com.olegpy" %% "better-monadic-for" % Versions.betterMonadicFor)
        val kindProjector = compilerPlugin(
      ("org.typelevel" %% "kind-projector" % Versions.kindProjector).cross(CrossVersion.full)
    )
  }

  val rootDependencies = Seq(
    compilerPlugin(CompilerPlugins.kindProjector),
    compilerPlugin(CompilerPlugins.betterMonadicFor),
    "org.typelevel" %% "squants" % "1.7.4",
    CompilerPlugins.kindProjector,
    Libraries.cats,
    Libraries.catsRetry,
    Libraries.redis4catsEffects,
    Libraries.redis4catsLog4cats,
    Libraries.http4sDsl,
    Libraries.http4sClient,
    Libraries.http4sServer,
    Libraries.http4sCirce,
    Libraries.http4sJwtAuth,
    Libraries.log4cats,
    Libraries.pureConfig,
    Libraries.refinedPureconfig,
    Libraries.circeCore,
    Libraries.circeGeneric,
    Libraries.circeParser,
    Libraries.circeRefined,
    Libraries.catsEffect,
    Libraries.catsMeowMtlCore,
    Libraries.catsMeowMtlEffects,
    Libraries.cirisCore,
    Libraries.cirisEnum,
    Libraries.cirisRefined,
    Libraries.derevoCats,
    Libraries.derevoCirce,
    Libraries.derevoCore,
    Libraries.fs2,
    Libraries.skunkCore,
    Libraries.skunkCirce,
    Libraries.newtype,
    Libraries.refinedCore,
    Libraries.refinedCats,
    Libraries.weaverCats,
    Libraries.weaverScalaCheck
  )

}
