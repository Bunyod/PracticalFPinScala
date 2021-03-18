import Dependencies._

name := "PracticalScalaFP"
version in ThisBuild := "0.0.1"

lazy val IntegrationTest = config("it") extend(Test)

lazy val root = (project in file("."))
  .enablePlugins(DockerPlugin)
  .enablePlugins(AshScriptPlugin)
  .enablePlugins(JavaAppPackaging)
  .settings(
    name := "PracticalFPinScala",
    scalaVersion := "2.13.3",
    mainClass in Compile := Some("bunyod.fp.MainIO"),
    Global / onChangedBuildSource := ReloadOnSourceChanges,
    scalacOptions ++= CompilerOptions.cOptions,
    scalafmtOnCompile := true,
    dockerBaseImage := "openjdk:8u201-jre-alpine3.9",
    dockerExposedPorts ++= Seq(8080),
    makeBatScripts := Seq(),
    dockerUpdateLatest := true,
    libraryDependencies ++= rootDependencies
  )
  .configs(IntegrationTest)
  .settings(Defaults.itSettings: _*)
