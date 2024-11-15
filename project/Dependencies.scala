import sbt.*

object Dependencies {

  object V {
    val GoogleCommonProtos = "2.9.6-0"
    val GrpcJava: String = scalapb.compiler.Version.grpcJavaVersion
    val GrpcRestGateway = "0.7.1"
    val Logback = "1.5.12"
    val Pekko = "1.1.2"
    val PekkoGrpc = "1.1.1"
    val PekkoHttp = "1.1.0"
    val PekkoManagement = "1.1.0"
    val Scala2_13 = "2.13.15"
    val Scala3 = "3.5.2"
    val ScalaPb: String = scalapb.compiler.Version.scalapbVersion
    val ScalaPbJson = "0.12.1"
  }

  val Commons: Seq[ModuleID] = Seq(
    "io.github.sfali23" %% "grpc-rest-gateway-runtime" % V.GrpcRestGateway,
    "com.thesamet.scalapb.common-protos" %% "proto-google-common-protos-scalapb_0.11" % V.GoogleCommonProtos % "compile,protobuf"
  )

  val PekkoGrpc: Seq[ModuleID] = Seq(
    "org.apache.pekko" %% "pekko-actor" % V.Pekko,
    "org.apache.pekko" %% "pekko-actor-typed" % V.Pekko,
    "org.apache.pekko" %% "pekko-stream-typed" % V.Pekko,
    "org.apache.pekko" %% "pekko-http" % V.PekkoHttp,
    "org.apache.pekko" %% "pekko-grpc-runtime" % V.PekkoGrpc,
    "com.thesamet.scalapb" %% "compilerplugin" % V.ScalaPb % "compile;protobuf",
    "com.thesamet.scalapb" %% "scalapb-runtime" % V.ScalaPb % "compile;protobuf",
    "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % V.ScalaPb,
    "com.thesamet.scalapb" %% "scalapb-json4s" % V.ScalaPbJson
  )

  val Client: Seq[ModuleID] = PekkoGrpc ++ Commons

  val Server: Seq[ModuleID] = PekkoGrpc ++ Seq(
    "io.grpc" % "grpc-netty" % V.GrpcJava
  ) ++ Commons

  val Service: Seq[ModuleID] = Seq(
    "org.apache.pekko" %% "pekko-cluster-typed" % V.Pekko,
    "org.apache.pekko" %% "pekko-remote" % V.Pekko,
    "org.apache.pekko" %% "pekko-discovery" % V.Pekko,
    "org.apache.pekko" %% "pekko-management" % V.PekkoManagement,
    "org.apache.pekko" %% "pekko-management-cluster-http" % V.PekkoManagement,
    "org.apache.pekko" %% "pekko-management-cluster-bootstrap" % V.PekkoManagement,
    "ch.qos.logback" % "logback-classic" % V.Logback
  )
}