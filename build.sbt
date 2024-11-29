import CommonSettings.*
import Dependencies.*
import org.apache.pekko.grpc.sbt.PekkoGrpcPlugin.autoImport.PekkoGrpc

lazy val api = project
  .in(file("api"))
  .configure(commonSettings)
  .settings(name := "petstore-grpc-api")

lazy val commons = project
  .in(file("commons"))
  .configure(commonSettings)
  .settings(
    name := "petstore-grpc-commons",
    buildInfoPackage := "com.improving.petstore.commons",
    libraryDependencies ++= CommonsModule
  )
  .dependsOn(api)

lazy val client = project
  .in(file("client"))
  .configure(commonSettings)
  .configure(pekkoGrpcSettings(api, Seq(PekkoGrpc.Server, PekkoGrpc.Client)))
  .settings(
    name := "petstore-grpc-client",
    buildInfoPackage := "com.improving.petstore.client",
    libraryDependencies ++= Client
  )
  .dependsOn(api)

lazy val `netty-gateway-service` = project
  .in(file("netty-gateway-service"))
  .configure(commonSettings)
  .settings(
    name := "petstore-netty-gateway-service",
    buildInfoPackage := "com.improving.petstore.netty_gateway",
    libraryDependencies ++= NettyGatewayService,
    (Compile / PB.protoSources) += (api / baseDirectory).value / "src" / "main" / "protobuf",
    Compile / PB.targets ++= Seq(
      scalapb.gen(scala3Sources = true) -> (Compile / sourceManaged).value / "scalapb",
      grpc_rest_gateway.gatewayGen(scala3Sources = true) -> (Compile / sourceManaged).value / "scalapb",
      grpc_rest_gateway.swaggerGen() -> (Compile / resourceDirectory).value / "specs"
    ),
    run / fork := true
  )
  .dependsOn(api, commons)

lazy val `pekko-gateway-service` = project
  .in(file("pekko-gateway-service"))
  .configure(commonSettings)
  .settings(
    name := "petstore-pekko-gateway-service",
    buildInfoPackage := "com.improving.petstore.pekko_gateway",
    libraryDependencies ++= PekkoGatewayService,
    (Compile / PB.protoSources) += (api / baseDirectory).value / "src" / "main" / "protobuf",
    Compile / PB.targets ++= Seq(
      grpc_rest_gateway
        .gatewayGen(scala3Sources = true, implementationType = "pekko") -> crossTarget.value / "pekko-grpc" / "main",
      grpc_rest_gateway.swaggerGen() -> (Compile / resourceDirectory).value / "specs"
    ),
    (Compile / sourceManaged) := crossTarget.value / "pekko-grpc" / "main",
    run / fork := true
  )
  .dependsOn(api, commons, client)

lazy val service = project
  .in(file("service"))
  .configure(commonSettings)
  .settings(
    name := "petstore-grpc-service",
    buildInfoPackage := "com.improving.petstore.service",
    libraryDependencies ++= Service,
    run / fork := true
  )
  .dependsOn(commons, client)

lazy val `petstore-grpc` = project
  .in(file("."))
  .configure(commonSettings)
  .aggregate(api, commons, client, service, `netty-gateway-service`, `pekko-gateway-service`)
