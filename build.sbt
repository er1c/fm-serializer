import sbt._
import ReleaseTransformations._
//import microsites.ExtraMdFileConfig
import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}

val JacksonVersion = "2.10.0"

lazy val buildSettings = Seq(
  organization := "io.github.er1c",
  scalaVersion := "2.13.2",
  crossScalaVersions := Seq("2.11.12", "2.12.11", scalaVersion.value),
  // Need to make sure any Java sources are compiled to 1.8 classfile format
  javacOptions ++= Seq("-source", "1.8", "-target", "1.8"),
  // Needed for the JavaBean tests to work, IntelliJ imports this weird,
  //   need to enable use SBT for build
  compileOrder := CompileOrder.JavaThenScala,
)

lazy val commonSettings = Seq(
  scalacOptions := Seq(
    "-encoding", "UTF-8",
    "-unchecked",
    "-deprecation",
    "-language:implicitConversions,experimental.macros",
    "-feature",
    "-Xlint",
    //"-Ylog-classpath",
    "-Xelide-below", "OFF", // ALL == Enabled Assertions,  OFF == Disabled Assertions
    "-Xfatal-warnings",     // Warnings become Errors
  ),
  scalacOptions ++= (
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, v)) if v <= 11 => Seq(
        "-Ypartial-unification",
        "-Ydelambdafy:inline",
        "-language:higherKinds",
        "-Ywarn-unused-import",
      )
      case Some((2, v)) if v >= 12 => Seq(
        "-opt:l:inline",
        "-opt-inline-from:<sources>",
        // Remove "params" since we often have method signatures that intentionally have the parameters, but may not be used in every implementation, also omit "patvars" since it isn't part of the default xlint:unused and isn't super helpful
        "-Ywarn-unused:imports,privates,locals",
      ) ++ (if (v == 12) Seq(
        "-Ypartial-unification",
        "-language:higherKinds",
      ) else Nil)
      case _ => Seq.empty
    }
  ),
  resolvers ++= Seq(
    Resolver.sonatypeRepo("releases"),
    Resolver.sonatypeRepo("snapshots")
  ),
  libraryDependencies ++= Seq(
    "org.scala-lang" % "scala-compiler" % scalaVersion.value,
    "org.scala-lang" % "scala-reflect" % scalaVersion.value,
    "org.scalatest" %% "scalatest" % "3.0.8" % Test,
  ),
  scmInfo :=
    Some(ScmInfo(
      url("https://github.com/er1c/scala-serializer"),
      "scm:git@github.com:er1c/scala-serializer.git"
    )),
  testOptions += Tests.Argument("-oF"),
  // We don't want log buffering when running ScalaTest
  logBuffered in Test := false,
  mimaPreviousArtifacts := Set(organization.value %% moduleName.value % "1.0.0"),
)

initialCommands in console := """import serializer._"""

lazy val commonJsSettings = Seq(
  scalaJSStage in Global := FastOptStage,
  parallelExecution in Test := false,
)

lazy val commonJvmSettings = Seq(
  parallelExecution in Test := false,
)

lazy val coreSettings = buildSettings ++ setCrossDirs(Test) ++ setCrossDirs(Compile) ++ commonSettings ++ publishSettings ++ releaseSettings

// Root Project
lazy val root = project.in(file("."))
  .aggregate(
    coreJS, coreJVM,
    commontypesJVM, commontypesJS,
    bsonJVM,
    jacksonJVM,
    jsonJVM, jsonJS,
    protobufJVM,
  )
  .dependsOn(
    commontypesJVM % "compile->compile;test->test",
    commontypesJS  % "compile->compile;test->test",
    bsonJVM        % "compile->compile;test->test",
    jacksonJVM     % "compile->compile;test->test",
    protobufJVM    % "compile->compile;test->test",
  )
  .settings(coreSettings:_*)
  .settings(noPublishSettings)

// Core Library
lazy val core = crossProject(JSPlatform, JVMPlatform).crossType(CrossType.Full)
  .settings(
    moduleName := "scala-serializer",
    description := "Scala Macro Based Serialization",
    libraryDependencies ++= Seq(
      // JAXB - Needed for Java 9+ since it is no longer automatically available
      //   Used for @XmlTransient annotation
      "com.sun.xml.bind" % "jaxb-core" % "2.3.0.1" % Test,
      "com.sun.xml.bind" % "jaxb-impl" % "2.3.1" % Test,
      "javax.xml.bind" % "jaxb-api" % "2.3.1" % Test,
      "javax.activation" % "javax.activation-api" % "1.2.0" % Test,
    ),
  )
  .settings(coreSettings:_*)
  .jsSettings(commonJsSettings:_*)
  .jvmSettings(commonJvmSettings:_*)

lazy val coreJVM = core.jvm
lazy val coreJS = core.js

// Serializer/Deserializer Implementations
lazy val commontypes = crossProject(JSPlatform, JVMPlatform).crossType(CrossType.Full)
  .settings(
    moduleName := "scala-serializer-commontypes",
    description := "Common java data type serializers for scala-serializer",
  )
  .dependsOn(core % "compile->compile;test->test")
  .settings(coreSettings:_*)
  .jsSettings(commonJsSettings:_*)
  .jvmSettings(commonJvmSettings:_*)

lazy val commontypesJVM = commontypes.jvm
lazy val commontypesJS = commontypes.js

// Serializer/Deserializer Implementations
lazy val bson = crossProject(JVMPlatform).crossType(CrossType.Full)
  .settings(
    moduleName := "scala-serializer-bson",
    description := "BSON serializer/deserializer for scala-serializer",
    libraryDependencies ++= Seq(
      "org.mongodb" % "bson" % "3.3.0",
    ),
  )
  .dependsOn(core % "compile->compile;test->test")
  .settings(coreSettings:_*)
  .jvmSettings(commonJvmSettings:_*)

lazy val bsonJVM = bson.jvm

lazy val jackson = crossProject(JVMPlatform).crossType(CrossType.Full)
  .settings(
    moduleName := "scala-serializer-jackson",
    description := "Jackson serializer/deserializer for scala-serializer",
    libraryDependencies ++= Seq(
      "com.fasterxml.jackson.core" % "jackson-annotations" % JacksonVersion,
      "com.fasterxml.jackson.core" % "jackson-core" % JacksonVersion,
      "com.fasterxml.jackson.core" % "jackson-databind" % JacksonVersion,
      "com.fasterxml.jackson.module" %% "jackson-module-scala" % JacksonVersion,
    ), // ++ jaxbLibraries,
  )
  .dependsOn(json % "compile->compile;test->test")
  .settings(coreSettings:_*)
  .jvmSettings(commonJvmSettings:_*)

lazy val jacksonJVM = jackson.jvm

lazy val json = crossProject(JSPlatform, JVMPlatform).crossType(CrossType.Full)
  .settings(
    moduleName := "scala-serializer-json",
    description := "JSON serializer/deserializer for scala-serializer",
  )
  .dependsOn(core % "compile->compile;test->test")
  .settings(coreSettings:_*)
  .jsSettings(commonJsSettings:_*)
  .jvmSettings(commonJvmSettings:_*)

lazy val jsonJVM = json.jvm
lazy val jsonJS = json.js

lazy val protobuf = crossProject(JVMPlatform).crossType(CrossType.Full)
  .settings(
    moduleName := "scala-serializer-protobuf",
    description := "Protobuf serializer/deserializer for scala-serializer",
  )
  .dependsOn(core % "compile->compile;test->test")
  .settings(coreSettings:_*)
  .jvmSettings(commonJvmSettings:_*)

lazy val protobufJVM = protobuf.jvm

lazy val publishSettings = Seq(
  homepage := Some(url("https://github.com/er1c/scala-serializer")),
  licenses := Seq("Apache 2" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt")),
  publishMavenStyle := true,
  publishArtifact in Test := false,
  pomIncludeRepository := { _ => false },
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (version.value.trim.endsWith("SNAPSHOT"))
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases"  at nexus + "service/local/staging/deploy/maven2")
  },
  developers := List(
    Developer(
      "ericpeters",
      "Eric Peters",
      "eric@peters.org",
      url("https://github.com/er1c")
    ),
    Developer(
      "timunderwood",
      "Tim Underwood",
      "timunderwood@gmail.com",
      url("https://github.com/tpunder")
    ),
  ),
)

lazy val noPublishSettings = Seq(
  publish := {},
  publishLocal := {},
  publishArtifact := false
)

lazy val releaseSettings = Seq(
  releaseCrossBuild := true,
  releasePublishArtifactsAction := PgpKeys.publishSigned.value,
  releaseProcess := Seq[ReleaseStep](
    checkSnapshotDependencies,
    inquireVersions,
    runClean,
    runTest,
    setReleaseVersion,
    commitReleaseVersion,
    tagRelease,
    publishArtifacts,
    setNextVersion,
    commitNextVersion,
    ReleaseStep(action = Command.process("sonatypeReleaseAll", _)),
    pushChanges
  )
)

credentials ++= (for {
  username <- Option(System.getenv().get("SONATYPE_USERNAME"))
  password <- Option(System.getenv().get("SONATYPE_PASSWORD"))
} yield Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", username, password)).toSeq

// Adds a `src/main/scala-2.13+` source directory for Scala 2.13 and newer
// and a `src/main/scala-2.12-` source directory for Scala version older than 2.13
// unmanagedSourceDirectories is "order sensitive" so override the default settings in the correct priority order
def setCrossDirs(config: Configuration): Seq[Setting[_]] = {
  Seq(
    unmanagedSourceDirectories in config := {
      val baseDir   = baseDirectory.value
      //val platform  = crossProjectPlatform.value.identifier

      val configPath: String = config match {
        case Compile => "main"
        case Test    => "test"
        case _       => return Nil
      }

      val javaSources = Seq(
        baseDir / ".."  / "shared" / "src" / configPath / "java",
        baseDir / "src" / configPath / "java",
      )

      val scalaSources = (CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, n)) if n < 13 => Seq(
          baseDir                    / "src" / configPath / "scala",       // for 2.x or 3.0
          baseDir / ".."  / "shared" / "src" / configPath / "scala",
          baseDir                    / "src" / configPath / "scala-2.12-", // for both 2.12 and 2.11
          baseDir / ".."  / "shared" / "src" / configPath / "scala-2.12-",
          baseDir                    / "src" / configPath / s"scala-2.$n", // explicit version (e.g. 2.11)
          baseDir / ".."  / "shared" / "src" / configPath / s"scala-2.$n",
        )
        case Some((m, n))           => Seq(
          baseDir                   / "src" / configPath / "scala",        // for 2.x or 3.0
          baseDir / ".." / "shared" / "src" / configPath / "scala",
          baseDir                   / "src" / configPath / "scala-2.13+",  // for 2.13 or above
          baseDir / ".." / "shared" / "src" / configPath / "scala-2.13+",
          baseDir                   / "src" / configPath / s"scala-$m.$n", // explicit version (e.g. 2.13)
          baseDir / ".." / "shared" / "src" / configPath / s"scala-$m.$n",
        )
      })

      javaSources ++ scalaSources
    }
  )
}