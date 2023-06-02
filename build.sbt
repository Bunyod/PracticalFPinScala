import Dependencies._

name := "PracticalScalaFP"
ThisBuild / version := "0.0.1"

lazy val IntegrationTest = config("it") extend(Test)

lazy val root = (project in file("."))
  .enablePlugins(DockerPlugin)
  .enablePlugins(AshScriptPlugin)
  .enablePlugins(JavaAppPackaging)
  .settings(
    name := "PracticalFPinScala",
    scalaVersion := "2.13.10",
    Compile / mainClass := Some("bunyod.fp.MainIO"),
    Global / onChangedBuildSource := ReloadOnSourceChanges,
//    scalacOptions ++= CompilerOptions.cOptions,
//    scalafmtOnCompile := true,
      scalacOptions ++= Seq(
          "-Xfatal-warnings", // New lines for each options
          "-deprecation",
          "-unchecked",
          "-language:implicitConversions",
          "-language:higherKinds",
          "-language:existentials",
          "-language:postfixOps",
          "-Ywarn-dead-code",
          "-Ywarn-numeric-widen",
          "-Xfatal-warnings",
          "-deprecation",
          "-Xlint:-unused,_",
          "-deprecation",
          "-Ymacro-annotations",
          "-Xmaxerrs",
          "200",
      ),
    dockerBaseImage := "openjdk:8u201-jre-alpine3.9",
    dockerExposedPorts ++= Seq(8080),
    makeBatScripts := Seq(),
    dockerUpdateLatest := true,
    libraryDependencies ++= rootDependencies
  )
  .configs(IntegrationTest)
  .settings(Defaults.itSettings: _*)
