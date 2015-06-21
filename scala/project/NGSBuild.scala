import sbt._
import Keys._

trait BuildSetting {
  // Eclipse plugin
  import com.typesafe.sbteclipse.plugin.EclipsePlugin._

  //Dependency graph plugin
  import net.virtualvoid.sbt.graph.Plugin._

  val scalaSettings =
    Defaults.defaultSettings ++
    eclipseSettings ++
    graphSettings ++ Seq(
      initialCommands := "import org.revenj.patterns._"
    , javaHome := sys.env.get("JDK16_HOME").map(file(_))
    , javacOptions := Seq(
        "-deprecation"
      , "-encoding", "UTF-8"
      , "-Xlint:unchecked"
      , "-source", "1.6"
      , "-target", "1.6"
      )
    , scalacOptions := Seq(
        "-deprecation"
      , "-encoding", "UTF-8"
      , "-feature"
      , "-language:existentials"
      , "-language:implicitConversions"
      , "-language:postfixOps"
      , "-language:reflectiveCalls"
      , "-optimise"
      , "-unchecked"
      , "-Xcheckinit"
      , "-Xlint"
      , "-Xmax-classfile-name", "72"
      , "-Xno-forwarders"
      , "-Xverify"
      , "-Yclosure-elim"
      , "-Ydead-code"
      , "-Yinline-warnings"
      , "-Yinline"
      , "-Yrepl-sync"
      , "-Ywarn-adapted-args"
      , "-Ywarn-dead-code"
      , "-Ywarn-inaccessible"
      , "-Ywarn-nullary-override"
      , "-Ywarn-nullary-unit"
      , "-Ywarn-numeric-widen"
      ) ++ (CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, 11)) =>
        Seq(
          "-Yconst-opt"
        , "-Ywarn-infer-any"
        , "-Ywarn-unused"
        )
        case _ =>
        Seq.empty
      })
    , publishArtifact in (Compile, packageDoc) := false
    , externalResolvers := Resolver.withDefaultResolvers(resolvers.value, mavenCentral = false)
    )
}

// ----------------------------------------------------------------------------

trait Dependencies {
  val jodaTime = "joda-time" % "joda-time" % "2.7"
  val jodaTimeConvert = "org.joda" % "joda-convert" % "1.2"

  // Pico container feat. context
  val picoContainer = "org.picocontainer" % "picocontainer" % "3.0.a4" classifier "ngs"

  // PgScala with PostgreSQL JDBC driver
  val pgscala = "org.pgscala" %% "pgscala" % "0.7.29"

  // Logging facade
  val slf4j = "org.slf4j" % "slf4j-api" % "1.7.12"

  // Jackson module for Scala
  val jacksonScala = "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.4.4"

  val jacksonDatabind = "com.fasterxml.jackson.core" % "jackson-databind" % "2.4.4"

  val scalaReflect = Def.setting { "org.scala-lang" % "scala-reflect" % scalaVersion.value }

  //test libs
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "2.2.4"
  lazy val jUnit = "junit" % "junit" % "4.12"

  // Logging
  val logback = "ch.qos.logback" % "logback-classic" % "1.1.3" % "compile->default"
}

// ----------------------------------------------------------------------------

object NGSBuild extends Build with Dependencies with BuildSetting {

  def serverProject(id: String, settings: scala.Seq[sbt.Def.Setting[_]]) = Project(
      id = id
    , base = file(id)
    , settings = settings ++ Seq(name := "revenj-scala-" + id.toLowerCase)
    )

  lazy val interfaces = serverProject(
   "Interfaces"
  , scalaSettings ++ Seq(
      libraryDependencies ++= Seq(
        jodaTime
      , jodaTimeConvert
      , scalaReflect.value
      )
    )
  )

  lazy val core = serverProject(
    "Core"
  , settings = scalaSettings ++ Seq(
      libraryDependencies ++= Seq(
        picoContainer
      , pgscala
      , slf4j
      , jacksonDatabind
      , jacksonScala
      )
    , unmanagedSourceDirectories in Compile :=
      (javaSource in Compile).value ::
      (scalaSource in Compile).value ::
      Nil
    )
  ) dependsOn(interfaces)

  lazy val tests = serverProject(
    "Tests"
  , scalaSettings ++ Seq(
      libraryDependencies ++= Seq(
        scalaTest % "test"
      , jUnit % "test"
      , logback
      )
    , unmanagedSourceDirectories in Compile :=
        (scalaSource in Compile).value ::
          sourceDirectory.value / "generated" / "scala" ::
          Nil
    , unmanagedSourceDirectories in Test :=
        (scalaSource in Test).value ::
          (javaSource in Test).value ::
          Nil
    )
  ) dependsOn(core)

  lazy val root = project in file(".") aggregate(core, interfaces) settings (packagedArtifacts := Map.empty)
}
