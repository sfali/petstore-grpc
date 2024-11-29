package com.improving
package petstore
package utils

import com.typesafe.config.{Config, ConfigFactory}

object LocalConfig {

  private val nettyGatewayConfig =
    s"""rest-gateway.service-port={grpc-port}
       |rest-gateway.gateway-port={gateway-port}
       |""".stripMargin
  private val pekkoGatewayConfig = s"""rest-gateway.port={gateway-port}""".stripMargin
  private val baseRemotePort = 17355
  private val baseManagementPort = 8558
  private val baseGrpcPort = 8080
  private val baseGatewayPort = 7070

  def apply(
    serviceName: String,
    nodeNum: Int,
    totalNumOfNodes: Int,
    remotePort: Int = baseRemotePort,
    managementPort: Int = baseManagementPort,
    grpcPort: Int = baseGrpcPort,
    gatewayPort: Int = baseGatewayPort,
    nettyImpl: Boolean = true
  ): Config = {
    require(nodeNum > 0 && nodeNum <= totalNumOfNodes)

    val discoveryConfig = (0 until totalNumOfNodes)
      .map { index =>
        s"""{host = "0.0.0.0", port = ${managementPort + index}}"""
      }
      .mkString(System.lineSeparator())

    val nextValue = nodeNum - 1
    val finalRemotePort = remotePort + nextValue
    val finalManagementPort = managementPort + nextValue
    val finalGrpcPort = grpcPort + nextValue
    val finalGatewayPort = gatewayPort + nextValue
    val gatewayConfig =
      if (nettyImpl)
        nettyGatewayConfig
          .replaceAll("\\{grpc-port}", finalGrpcPort.toString)
          .replaceAll("\\{gateway-port}", finalGatewayPort.toString)
      else pekkoGatewayConfig.replaceAll("\\{gateway-port}", finalGatewayPort.toString)

    val pekkoManagementConfig =
      if (managementPort != baseManagementPort) s""""""
      else ""

    val config =
      s"""pekko.discovery.config.services {
         |  "$serviceName" {
         |    endpoints = [
         |      $discoveryConfig
         |    ]
         |  }
         |}
         |
         |pekko.http.server.preview.enable-http2=on
         |pekko.management.http.bind-port=\"\"
         |pekko.remote.artery.bind.port=\"\"
         |pekko.actor.provider=cluster
         |pekko.management.cluster.bootstrap.contact-point-discovery.service-name=$serviceName$pekkoManagementConfig
         |pekko.remote.artery.canonical.port=$finalRemotePort
         |pekko.management.http.port=$finalManagementPort
         |app.grpc-settings.port=$grpcPort
         |$gatewayConfig""".stripMargin

    ConfigFactory
      .parseString(config)
      .withFallback(ConfigFactory.load("local-shared"))
      .withFallback(ConfigFactory.load())
  }
}
