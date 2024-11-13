package com.improving
package petstore
package utils

import com.typesafe.config.{Config, ConfigFactory}

object LocalConfig {

  private val baseRemotePort = 17355
  private val baseManagementPort = 8558
  private val baseHttpPort = 8080
  private val baseGatewayPort = 7070

  def apply(nodeNum: Int, totalNumOfNodes: Int): Config = {
    require(nodeNum > 0 && nodeNum <= totalNumOfNodes)

    val discoveryConfig = (0 until totalNumOfNodes)
      .map { index =>
        s"""{host = "0.0.0.0", port = ${baseManagementPort + index}}"""
      }
      .mkString(System.lineSeparator())

    val nextValue = nodeNum - 1
    val remotePort = baseRemotePort + nextValue
    val managementPort = baseManagementPort + nextValue
    val grpcPort = baseHttpPort + nextValue
    val gatewayPort = baseGatewayPort + nextValue
    val config =
      s"""pekko.discovery.config.services {
         |  "petstore-grpc" {
         |    endpoints = [
         |      $discoveryConfig
         |    ]
         |  }
         |}
         |
         |pekko.remote.artery.canonical.port = $remotePort
         |pekko.management.http.port = $managementPort
         |app.grpc-settings.port=$grpcPort
         |rest-gateway.service-port=$grpcPort
         |rest-gateway.gateway-port=$gatewayPort""".stripMargin

    ConfigFactory
      .parseString(config)
      .withFallback(ConfigFactory.load("local-shared"))
  }
}
