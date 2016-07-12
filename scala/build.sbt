// ### PROJECT SETTINGS ###

lazy val core = (project in file("revenj-core")
  settings(commonSettings ++ publishSettings)
  settings(
    version := "0.1.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      "org.postgresql" % "postgresql" % "9.4.1208",
      "org.scala-lang" % "scala-reflect" % scalaVersion.value,
      "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.7.4" % "test",
      "org.specs2" %% "specs2-scalacheck" % "3.8.3" % "test"
    )
  )
)

lazy val storage = (project in file("revenj-storage")
  settings(commonSettings ++ publishSettings)
  settings(
    version := "0.1.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      "org.specs2" %% "specs2-scalacheck" % "3.8.3" % "test"
    )
  )
)

lazy val root = (project in file(".")
  settings(commonSettings)
  settings(
    name := "revenj",
    version := "0.0.0",
    publishLocal := {},
    publish := {}
  )
  aggregate(core, storage)
)

// ### COMMON SETTINGS ###

lazy val commonSettings = Defaults.coreDefaultSettings ++ Seq(
  organization := "net.revenj",
  name := baseDirectory.value.getName,

  scalaVersion := crossScalaVersions.value.head,
  crossScalaVersions := Seq("2.11.8", "2.12.0-M5"),

  scalacOptions ++= Seq(
    "-deprecation",
    "-encoding", "UTF-8",
    "-feature",
    "-language:_",
    "-unchecked",
    "-Xfuture",
    "-Xlint",
    "-Xno-forwarders",
    "-Xverify",
    "-Yrepl-sync",
    "-Ywarn-adapted-args",
    "-Ywarn-dead-code",
    "-Ywarn-inaccessible",
    "-Ywarn-infer-any",
    "-Ywarn-nullary-override",
    "-Ywarn-nullary-unit",
    "-Ywarn-numeric-widen",
    "-Ywarn-unused"
  ) ++ (CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, 11)) => Seq(
        "-optimise",
        "-Yclosure-elim",
        "-Yconst-opt",
        "-Ydead-code",
        "-Yinline",
        "-Yinline-warnings"
      )
    case _ => Seq(
        "-Yopt:_"
      )
  }),

  unmanagedSourceDirectories in Compile := Seq((scalaSource in Compile).value),
  unmanagedSourceDirectories in Test := Seq((scalaSource in Test).value)
)

// ### PUBLISH SETTINGS ###

val publishSettings = Seq(
  scalacOptions in (Compile, doc) ++= Seq(
    "-no-link-warnings",
    "-sourcepath", baseDirectory.value.toString,
    "-doc-source-url", if (isSnapshot.value) {
      s"""https://github.com/ngs-doo/revenj/tree/master/scala/${name.value}\u20AC{FILE_PATH}.scala"""
    } else {
      s"""https://github.com/ngs-doo/revenj/blob/${version.value}/scala/${name.value}\u20AC{FILE_PATH}.scala"""
    }
  ),

  packageOptions := Seq(Package.ManifestAttributes(
    ("Implementation-Vendor", "Nova Generacija Softvera d.o.o."),
    ("Sealed", "true")
  )),

  publishTo := Some(if (isSnapshot.value) Opts.resolver.sonatypeSnapshots else Opts.resolver.sonatypeStaging),
  publishArtifact in Test := false,
  publishMavenStyle := true,
  pomIncludeRepository := { _ => false },

  pomExtra :=
    <inceptionYear>2016</inceptionYear>
    <url>https://github.com/ngs-doo/revenj/tree/master/scala</url>
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
        <name>Rikard Paveli&#263;</name>
        <url>https://github.com/zapov</url>
      </developer>
      <developer>
        <id>melezov</id>
        <name>Marko Elezovi&#263;</name>
        <url>https://github.com/melezov</url>
      </developer>
    </developers>
)
