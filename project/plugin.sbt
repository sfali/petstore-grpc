addSbtPlugin("com.thesamet" % "sbt-protoc" % "1.0.7")
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.4.6")
addSbtPlugin("org.apache.pekko" % "pekko-grpc-sbt-plugin" % "1.1.0")
addSbtPlugin("com.thesamet" % "sbt-protoc-gen-project" % "0.1.6")
addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.10.0")
addSbtPlugin("com.awwsmm.sbt" % "sbt-dependency-updater" % "0.4.0")
addDependencyTreePlugin

libraryDependencies ++= Seq(
  "com.thesamet.scalapb" %% "compilerplugin" % "0.11.17",
  "io.github.sfali23" %% "grpc-rest-gateway-code-gen" % "0.6.0"
)
