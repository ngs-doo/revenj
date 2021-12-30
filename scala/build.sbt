import com.dslplatform.compiler.client.parameters.{Settings, Targets}

lazy val DSL = SbtDslPlatformPlugin.autoImport.dsl

lazy val core = (project in file("revenj-core")
  settings (commonSettings ++ publishSettings)
  enablePlugins(SbtDslPlatformPlugin)
  settings(
    version := "1.3.0",
    libraryDependencies ++= Seq(
      "org.postgresql" % "postgresql" % "42.3.1",
      "joda-time" % "joda-time" % "2.10.13",   // TODO: will be removed
      "org.joda" % "joda-convert" % "2.2.1", // TODO: will be removed
      "org.scala-lang" % "scala-reflect" % scalaVersion.value,
      "io.monix" %% "monix-reactive" % "3.4.0",
      "org.scala-lang.modules" %% "scala-xml" % "1.2.0",
      "com.dslplatform" %% "dsl-json-scala" % "1.9.9",
      "com.dslplatform" % "dsl-json-joda" % "1.9.9", // TODO: will be removed
      "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.13.1",
      "com.fasterxml.jackson.datatype" % "jackson-datatype-joda" % "2.13.1",
      "com.fasterxml.jackson.datatype" % "jackson-datatype-jdk8" % "2.13.1",
      "com.fasterxml.jackson.datatype" % "jackson-datatype-jsr310" % "2.13.1",
      "org.specs2" %% "specs2-scalacheck" % "4.13.0" % Test,
    ),
    dslResourcePath in (Test, DSL) := Some((resourceDirectory in Test).value / "META-INF" / "services")
  )
)

lazy val akka = (project in file("revenj-akka")
  settings (commonSettings ++ publishSettings)
  settings(
  version := "1.3.0",
  libraryDependencies ++= Seq(
      "com.typesafe" % "config" % "1.3.4",
      "com.typesafe.akka" %% "akka-http" % "10.2.7",
      "com.typesafe.akka" %% "akka-stream" % "2.6.17"
    )
  )
  dependsOn(core)
)

lazy val storage = (project in file("revenj-storage")
  settings (commonSettings ++ publishSettings)
  settings(
    version := "1.3.0",
    libraryDependencies ++= Seq(
      "com.amazonaws" % "aws-java-sdk-s3" % "1.12.115",
      "org.specs2" %% "specs2-scalacheck" % "4.13.0" % Test,
    )
  )
  dependsOn(core)
)

lazy val tests = (project in file("tests")
  enablePlugins(SbtDslPlatformPlugin)
  settings (commonSettings ++ Seq(publishTo := Some(Opts.resolver.mavenLocalFile)))
  dependsOn(core)
  settings(
    name := "integration-tests",
    version := "0.0.0",
    libraryDependencies ++= Seq(
      "com.dslplatform" % "dsl-clc" % "1.9.10" % Test,
      "org.specs2" %% "specs2-scalacheck" % "4.7.1" % Test,
      "org.pgscala.embedded" %% "pgscala-embedded" % "0.3.0-0-SNAPSHOT" % Test,
      "ch.qos.logback" % "logback-classic" % "1.2.7" % Test,
    ),
    resolvers += Resolver.sonatypeRepo("snapshots"), // remove after pgscala-embedded confirmed working
    dslNamespace in (Test, DSL) := "example",
    dslDslPath in (Test, DSL) := Seq((resourceDirectory in Test).value),
    dslSettings in (Test, DSL) := Seq(Settings.Option.JACKSON, Settings.Option.JODA_TIME, Settings.Option.URI_REFERENCE),
    dslSources in (Test, DSL) += (Targets.Option.REVENJ_SCALA -> sourceManaged.value),
    dslResourcePath in (Test, DSL) := Some((resourceDirectory in Test).value / "META-INF" / "services"),
    dslAnsi in (Test, DSL) := false,
    publishLocal := {},
    publish := {},
    publishArtifact := false
  )
)

lazy val root = (project in file(".")
  settings (commonSettings ++ Seq(publishTo := Some(Opts.resolver.mavenLocalFile)))
  settings(
    name := "revenj",
    version := "0.0.0",
    publishLocal := {},
    publish := {},
    publishArtifact := false
  )
  aggregate(core, akka, storage, tests)
)

// ### COMMON SETTINGS ###

lazy val commonSettings = Defaults.coreDefaultSettings ++ Seq(
  organization := "net.revenj",
  name := baseDirectory.value.getName,
  //resolvers += Resolver.mavenLocal,

  scalaVersion := crossScalaVersions.value.head,
  crossScalaVersions := Seq("2.12.15", "2.13.7"),
  scalacOptions ++= Seq(
    "-deprecation",
    "-encoding", "UTF-8",
    "-feature",
    "-language:_",
    "-target:jvm-1.8",
    "-unchecked",
    "-Xfuture",
    "-Xlint:_",
    "-Xverify",
    "-Yrangepos",
    "-Ywarn-dead-code",
    "-Ywarn-numeric-widen",
    "-Ywarn-unused",
    "-opt:_"
  ),

  unmanagedSourceDirectories in Compile := Seq((javaSource in Compile).value) ++ Seq((scalaSource in Compile).value),
  unmanagedSourceDirectories in Test := Seq((scalaSource in Test).value)
)

// ### PUBLISH SETTINGS ###

val publishSettings = Seq(
  scalacOptions in(Compile, doc) ++= Seq(
    "-no-link-warnings",
    "-sourcepath", baseDirectory.value.toString,
    "-doc-source-url", if (isSnapshot.value) {
      s"""https://github.com/ngs-doo/revenj/tree/master/scala/${name.value}\u20AC{FILE_PATH}.scala"""
    } else {
      s"""https://github.com/ngs-doo/revenj/blob/${version.value}/scala/${name.value}\u20AC{FILE_PATH}.scala"""
    }
  ),

  packageOptions := Seq(Package.ManifestAttributes(
    ("Implementation-Vendor", "New Generation Software Ltd.")
  )),

  publishTo := Some(if (isSnapshot.value) Opts.resolver.sonatypeSnapshots else Opts.resolver.sonatypeStaging),
  publishArtifact in Test := false,
  publishMavenStyle := true,
  pomIncludeRepository := { _ => false },

  credentials ++= {
    val creds = Path.userHome / ".config" / "revenj" / "nexus.config"
    if (creds.exists) Some(Credentials(creds)) else None
  }.toSeq,

  pomExtra :=
    <inceptionYear>2016</inceptionYear>
      <url>https://github.com/ngs-doo/revenj</url>
      <licenses>
        <license>
          <name>BSD 3-clause "New" or "Revised" License</name>
          <url>https://spdx.org/licenses/BSD-3-Clause.html</url>
          <distribution>repo</distribution>
        </license>
      </licenses>
      <scm>
        <url>git@github.com:ngs-doo/revenj.git</url>
        <connection>scm:git:git@github.com:ngs-doo/revenj.git</connection>
      </scm>
      <developers>
        <developer>
          <id>zapov</id>
          <name>Rikard Paveli&#263;
          </name>
          <url>https://github.com/zapov</url>
        </developer>
        <developer>
          <id>melezov</id>
          <name>Marko Elezovi&#263;
          </name>
          <url>https://github.com/melezov</url>
        </developer>
      </developers>
)
