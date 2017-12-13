FMPublic

name := "fm-serializer"

description := "Scala Macro Based Serialization"

scalaVersion := "2.12.4"

crossScalaVersions := Seq("2.11.11", "2.12.4")

// Needed for the JavaBean tests to work
compileOrder := CompileOrder.JavaThenScala

// NOTE: For -Xelide-below:  ALL == Enabled Assertions,  OFF == Disabled Assertions
scalacOptions := Seq(
  "-unchecked",
  "-deprecation",
  "-language:implicitConversions,experimental.macros",
  "-feature",
  "-Xlint",
  "-Ywarn-unused-import",
  "-Xelide-below", "OFF"
) ++ (if (scalaVersion.value.startsWith("2.12")) Seq(
  // Scala 2.12 specific compiler flags
  "-opt:l:inline",
  "-opt-inline-from:<sources>"
) else Nil)

// We don't want log buffering when running ScalaTest
logBuffered in Test := false

libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value

// Enable the Macro Paradise Compiler Plugin for Scala 2.10
libraryDependencies ++= {
  if (scalaVersion.value.startsWith("2.10")) Seq (
    compilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full),
    "org.scalamacros" %% "quasiquotes" % "2.1.0"
  ) else Nil
}

// SCALA Libraries
libraryDependencies ++= Seq(
  "com.frugalmechanic" %% "fm-common" % "0.9.0"
)

// JAVA Libraries
libraryDependencies ++= Seq(
  "joda-time" % "joda-time" % "2.9.1",
  "org.joda" % "joda-convert" % "1.8", // Required by joda-time when using Scala
  "org.mongodb" % "bson" % "3.3.0"
)

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.0" % "test"
