import CommonSettings.*
import Dependencies.*
import org.apache.pekko.grpc.sbt.PekkoGrpcPlugin.autoImport.PekkoGrpc

lazy val api = project
  .in(file("api"))
  .configure(commonSettings)
  .settings(name := "petstore-grpc-api")

lazy val client = project
  .in(file("client"))
  .configure(commonSettings)
  .configure(pekkoGrpcSettings(api, Seq(PekkoGrpc.Client)))
  .settings(
    name := "petstore-grpc-client",
    buildInfoPackage := "com.improving.petstore.client",
    libraryDependencies := Client
  )
  .dependsOn(api)

lazy val server = project
  .in(file("server"))
  .configure(commonSettings)
  .configure(pekkoGrpcSettings(api, Seq(PekkoGrpc.Server)))
  .settings(
    name := "petstore-grpc-server",
    buildInfoPackage := "com.improving.petstore.server",
    libraryDependencies := Server,
    Compile / PB.targets ++= Seq(
      grpc_rest_gateway.gatewayGen(scala3Sources = true) -> crossTarget.value / "pekko-grpc" / "main",
      grpc_rest_gateway.swaggerGen() -> (Compile / resourceDirectory).value / "specs"
    )
  )
  .dependsOn(api)

lazy val service = project
  .in(file("service"))
  .configure(commonSettings)
  .settings(
    name := "petstore-grpc-service",
    buildInfoPackage := "com.improving.petstore.service",
    libraryDependencies := Service,
    run / fork := true
  )
  .dependsOn(server)

lazy val `petstore-grpc` = project
  .in(file("."))
  .configure(commonSettings)
  .aggregate(api, client, server, service)
