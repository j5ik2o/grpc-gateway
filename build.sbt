import scalapb.compiler.Version.{grpcJavaVersion, scalapbVersion, protobufVersion}

val baseSettings = Seq(
  sonatypeProfileName := "com.github.j5ik2o",
  organization := "com.github.j5ik2o",
  scalaVersion := "2.13.1",
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
  pomIncludeRepository := { _ =>
    false
  },
  pomExtra := {
    <url>https://github.com/j5ik2o/grpc-gateway</url>
      <licenses>
        <license>
          <name>The MIT License</name>
          <url>http://opensource.org/licenses/MIT</url>
        </license>
      </licenses>
      <scm>
        <url>git@github.com:j5ik2o/grpc-gateway.git</url>
        <connection>scm:git:github.com/j5ik2o/grpc-gateway</connection>
        <developerConnection>scm:git:git@github.com:j5ik2o/grpc-gateway.git</developerConnection>
      </scm>
      <developers>
        <developer>
          <id>j5ik2o</id>
          <name>Junichi Kato</name>
        </developer>
      </developers>
  },
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
    scalaVersion := "2.12.8",
    libraryDependencies ++= Seq(
      "com.thesamet.scalapb" %% "compilerplugin"            % scalapbVersion,
      "com.thesamet.scalapb" %% "scalapb-runtime-grpc"      % scalapbVersion,
      "com.google.protobuf"  % "protobuf-java"              % protobufVersion,
      "com.google.api.grpc"  % "proto-google-common-protos" % "1.17.0"
    ),
    PB.protoSources in Compile += target.value / "protobuf_external",
    includeFilter in PB.generate := new SimpleFilter(
      file => file.endsWith("annotations.proto") || file.endsWith("http.proto")
    ),
    PB.targets in Compile += PB.gens.java -> (sourceManaged in Compile).value
  )

lazy val runtime = (project in file("runtime"))
  .settings(baseSettings)
  .settings(
    name := "grpc-gateway-runtime",
    crossScalaVersions := Seq("2.12.8", "2.13.1"),
    libraryDependencies ++= Seq(
      "commons-io"           % "commons-io"                 % "2.6",
      "com.thesamet.scalapb" %% "compilerplugin"            % scalapbVersion,
      "com.thesamet.scalapb" %% "scalapb-runtime-grpc"      % scalapbVersion,
      "com.thesamet.scalapb" %% "scalapb-json4s"            % "0.10.1",
      "io.grpc"              % "grpc-all"                   % grpcJavaVersion,
      "org.webjars"          % "swagger-ui"                 % "3.19.4",
      "com.google.protobuf"  % "protobuf-java"              % protobufVersion,
      "com.google.api.grpc"  % "proto-google-common-protos" % "1.17.0"
    ),
    PB.protoSources in Compile += target.value / "protobuf_external",
    includeFilter in PB.generate := new SimpleFilter(
      file => file.endsWith("annotations.proto") || file.endsWith("http.proto")
    ),
    PB.targets in Compile += scalapb.gen() -> (sourceManaged in Compile).value,
    mappings in (Compile, packageBin) ++= Seq(
      baseDirectory.value / "target" / "protobuf_external" / "google" / "api" / "annotations.proto" -> "google/api/annotations.proto",
      baseDirectory.value / "target" / "protobuf_external" / "google" / "api" / "http.proto"        -> "google/api/http.proto"
    )
  )

val root = (project in file("."))
  .settings(baseSettings)
  .aggregate(generator, runtime)