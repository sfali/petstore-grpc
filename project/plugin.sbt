addSbtPlugin("com.thesamet" % "sbt-protoc" % "1.0.7")
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.5.2")
addSbtPlugin("com.github.sbt" % "sbt-native-packager" % "1.10.0")
addSbtPlugin("org.apache.pekko" % "pekko-grpc-sbt-plugin" % "1.1.1")
addSbtPlugin("com.thesamet" % "sbt-protoc-gen-project" % "0.1.6")
addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.13.1")
addSbtPlugin("com.awwsmm.sbt" % "sbt-dependency-updater" % "0.4.0")
addDependencyTreePlugin

libraryDependencies ++= Seq(
  "com.thesamet.scalapb" %% "compilerplugin" % "0.11.17",
  "io.github.sfali23" %% "grpc-rest-gateway-code-gen" % "0.8.1"
)

resolvers ++= Seq(
  "Sonatype OSS" at "https://s01.oss.sonatype.org/content/groups/public/"
)
