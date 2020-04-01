import Dependencies._
name := "PracticalScalaFP"

version := "0.1"

scalaVersion := "2.13.1"

lazy val root = (project in file("."))
  .settings(
    name := "examples",
    Global / onChangedBuildSource := ReloadOnSourceChanges,
    scalacOptions ++= CompilerOptions.compilerOptions,
//    scalafmtOnCompile := true,
    libraryDependencies ++= Seq(
      CompilerPlugins.betterMonadicFor,
      "org.typelevel" %% "squants" % "1.6.0",
      CompilerPlugins.contextApplied,
      CompilerPlugins.kindProjector,
      Libraries.cats,
      Libraries.catsRetry,
      Libraries.http4sDsl,
      Libraries.http4sClient,
      Libraries.http4sServer,
      Libraries.http4sCirce,
      Libraries.http4sJwtAuth,
      Libraries.log4cats,
      Libraries.circeCore,
      Libraries.circeGeneric,
      Libraries.circeParser,
      Libraries.circeRefined,
      Libraries.catsEffect,
      Libraries.catsMeowMtlCore,
      Libraries.catsMeowMtlEffects,
      Libraries.console4cats,
      Libraries.derevoCats,
      Libraries.derevoTagless,
      Libraries.fs2,
      Libraries.skunk,
      Libraries.monocleCore,
      Libraries.monocleMacro,
      Libraries.newtype,
      Libraries.refinedCore
    )
  )
