import Dependencies._

name := "PracticalScalaFP"

version in ThisBuild := "0.0.1"

lazy val root = (project in file("."))
  .settings(
    name := "PracticalFPinScala",
    scalaVersion := "2.13.3",
    Global / onChangedBuildSource := ReloadOnSourceChanges,
    scalacOptions ++= CompilerOptions.cOptions,
//      scalacOptions ++= List("-Ywarn-unused", "-Ywarn-value-discard"),
      scalafmtOnCompile := true,
      libraryDependencies ++= rootDependencies
  )
  .configs(IntegrationTest)
  .settings(Defaults.itSettings)
