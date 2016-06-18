name := "revenj"

version := "0.1"

scalaVersion := "2.11.8"

lazy val core = project in file("revenj-core")

libraryDependencies ++= Seq(
  "org.postgresql" % "postgresql" % "9.4.1208.jre6",
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.7.4"
)