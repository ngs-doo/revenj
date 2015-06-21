resolvers := Seq(
  "NGS Nexus" at "http://ngs.hr/nexus/content/groups/public/"
, Resolver.url("NGS Nexus (Ivy)", url("http://ngs.hr/nexus/content/groups/public/"))(Resolver.ivyStylePatterns)
)

externalResolvers := Resolver.withDefaultResolvers(resolvers.value, mavenCentral = false)

addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "3.0.0")

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.7.5")
