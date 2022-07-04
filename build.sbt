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
      scalaVersion := "2.13.5",
      Compile / mainClass := Some("bunyod.fp.MainIO"),
      Global / onChangedBuildSource := ReloadOnSourceChanges,
      scalacOptions ++= (
        if (scalaVersion.value.startsWith("3")) Seq("-explaintypes", "-Wunused", "-Ykind-projector")
        else Seq(
          "-Xfatal-warnings",
          "-deprecation",
          "-unchecked",
          "-language:implicitConversions",
          "-language:higherKinds",
          "-language:existentials",
          "-language:postfixOps",
          "-Ywarn-dead-code",
          "-Ywarn-numeric-widen",
          "-Ywarn-unused",
          "-Xfatal-warnings",
          "-deprecation",
          "-Xlint:-unused,_",
          "-deprecation",
          "-Ymacro-annotations",
          "-Xlint:adapted-args",
          "-Xmaxerrs",
          "200",
        )
      ),
      //    scalafmtOnCompile := true, ,
      dockerBaseImage := "openjdk:8u201-jre-alpine3.9",
      dockerExposedPorts ++= Seq(8080),
      makeBatScripts := Seq(),
      dockerUpdateLatest := true,
      libraryDependencies ++= rootDependencies,
      libraryDependencies ++= List(
          "ch.epfl.scala" % "scalafix-interfaces" % "0.10.1",
          ("ch.epfl.scala" %% "scalafix-rules"      % "0.10.1" % Test).cross(CrossVersion.for3Use2_13),
          ("com.github.xuwei-k" % "scalafix-rules" % "0.2.1").cross(CrossVersion.for3Use2_13)
      ) ++ ( if (scalaVersion.value.startsWith("3")) { Seq() } else {
        Seq(
          compilerPlugin(
            ("org.typelevel" %% "kind-projector" % Dependencies.Versions.kindProjector).cross(CrossVersion.full)
          )
        )
      }),
      semanticdbEnabled := true
  )
  .configs(IntegrationTest)
  .settings(Defaults.itSettings: _*)