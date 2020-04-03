import scalapb.compiler.Version.{ grpcJavaVersion, protobufVersion, scalapbVersion }

val scala210Version = "2.10.7"
val scala211Version = "2.11.12"
val scala212Version = "2.12.10"
val scala213Version = "2.13.1"

val baseSettings = Seq(
  sonatypeProfileName := "com.github.j5ik2o",
  organization := "com.github.j5ik2o",
  organizationHomepage := Some(url("https://github.com/j5ik2o")),
  scalacOptions ++= {
    Seq(
      "-feature",
      "-deprecation",
      "-unchecked",
      "-encoding",
      "UTF-8",
      "-language:_",
      "-target:jvm-1.8"
    )
  },
  parallelExecution in Test := false,
  scalafmtOnCompile in ThisBuild := true,
  publishMavenStyle := true,
  publishArtifact in Test := false,
  pomIncludeRepository := { _ => false },
  licenses := Seq("MIT" -> url("https://raw.githubusercontent.com/j5ik2o/grpc-gateway/master/LICENSE")),
  homepage := Some(url("https://github.com/j5ik2o/grpc-gateway")),
  scmInfo := Some(
      ScmInfo(
        browseUrl = url("https://github.com/j5ik2o/grpc-gateway"),
        connection = "scm:git:git@github.com:j5ik2o/grpc-gateway.git"
      )
    ),
  developers := List(
      Developer(id = "j5ik2o", name = "Junichi Kato", email = "j5ik2o@gmail.com", url = url("https://blog.j5ik2o.me"))
    ),
  publishTo := sonatypePublishToBundle.value,
  credentials := {
    val ivyCredentials = (baseDirectory in LocalRootProject).value / ".credentials"
    val gpgCredentials = (baseDirectory in LocalRootProject).value / ".gpgCredentials"
    Credentials(ivyCredentials) :: Credentials(gpgCredentials) :: Nil
  }
)

lazy val generator = (project in file("generator"))
  .settings(baseSettings)
  .settings(
    name := "grpc-gateway-generator",
    scalaVersion := scala212Version,
    crossScalaVersions := Seq(scala212Version, scala213Version),
    libraryDependencies ++= Seq(
        "com.thesamet.scalapb" %% "compilerplugin"            % scalapbVersion,
        "com.thesamet.scalapb" %% "scalapb-runtime-grpc"      % scalapbVersion,
        "com.google.protobuf"  % "protobuf-java"              % protobufVersion,
        "com.google.api.grpc"  % "proto-google-common-protos" % "1.17.0"
      ),
    PB.protoSources in Compile += target.value / "protobuf_external",
    includeFilter in PB.generate := new SimpleFilter(file =>
        file.endsWith("annotations.proto") || file.endsWith("http.proto")
      ),
    PB.targets in Compile += PB.gens.java -> (sourceManaged in Compile).value
  )

lazy val runtime = (project in file("runtime"))
  .settings(baseSettings)
  .settings(
    name := "grpc-gateway-runtime",
    scalaVersion := scala213Version,
    crossScalaVersions := Seq(scala212Version, scala213Version),
    libraryDependencies ++= Seq(
        "commons-io"           % "commons-io"                 % "2.6",
        "com.thesamet.scalapb" %% "compilerplugin"            % scalapbVersion,
        "com.thesamet.scalapb" %% "scalapb-runtime-grpc"      % scalapbVersion,
        "com.thesamet.scalapb" %% "scalapb-json4s"            % "0.10.1",
        "io.grpc"              % "grpc-all"                   % grpcJavaVersion,
        "org.webjars"          % "swagger-ui"                 % "3.19.4",
        "com.google.protobuf"  % "protobuf-java"              % protobufVersion,
        "com.google.api.grpc"  % "proto-google-common-protos" % "1.17.0",
        "javax.activation"     % "javax.activation-api"       % "1.2.0"
      ),
    PB.protoSources in Compile += target.value / "protobuf_external",
    includeFilter in PB.generate := new SimpleFilter(file =>
        file.endsWith("annotations.proto") || file.endsWith("http.proto")
      ),
    PB.targets in Compile += scalapb.gen() -> (sourceManaged in Compile).value,
    mappings in (Compile, packageBin) ++= Seq(
        baseDirectory.value / "target" / "protobuf_external" / "google" / "api" / "annotations.proto" -> "google/api/annotations.proto",
        baseDirectory.value / "target" / "protobuf_external" / "google" / "api" / "http.proto"        -> "google/api/http.proto"
      )
  )

val root = (project in file("."))
  .settings(baseSettings)
  .settings(
    name := "grpc-gateway-root",
    scalaVersion := scala212Version
  )
  .aggregate(generator, runtime)
