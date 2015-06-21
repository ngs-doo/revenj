val NGSNexus     = "NGS Nexus"     at "http://ngs.hr/nexus/content/groups/public/"
val NGSReleases  = "NGS Releases"  at "http://ngs.hr/nexus/content/repositories/releases/"
val NGSSnapshots = "NGS Snapshots" at "http://ngs.hr/nexus/content/repositories/snapshots/"

organization in ThisBuild := "org.revenj"
version in ThisBuild      := "0.5.0"

resolvers in ThisBuild := Seq(NGSNexus)
publishTo in ThisBuild := Some(if (version.value endsWith "-SNAPSHOT") NGSSnapshots else NGSReleases)

credentials in ThisBuild  ++= {
  val creds = Path.userHome / ".config" / "revenj-scala" / "nexus.config"
  if (creds.exists) Some(Credentials(creds)) else None
}.toSeq

crossScalaVersions in ThisBuild := Seq("2.10.5", "2.11.6")
scalaVersion in ThisBuild       := crossScalaVersions.value.last
