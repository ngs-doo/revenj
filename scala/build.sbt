organization := "net.revenj"
name := "revenj"

lazy val core = project in file("revenj-core") settings(
  libraryDependencies ++= Seq(
    "org.postgresql" % "postgresql" % "9.4.1208",
    "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.7.4"
  )
)
