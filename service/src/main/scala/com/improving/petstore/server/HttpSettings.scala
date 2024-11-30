package com.improving
package petstore
package server

import com.typesafe.config.Config

import scala.concurrent.duration.*
import scala.jdk.DurationConverters.*

case class HttpSettings(host: String, port: Int, hardTerminationDeadline: FiniteDuration)

object HttpSettings {
  def apply(host: String, port: Int, hardTerminationDeadline: FiniteDuration): HttpSettings =
    new HttpSettings(host, port, hardTerminationDeadline)

  def apply(config: Config): HttpSettings =
    HttpSettings(
      host = config.getString("host"),
      port = config.getInt("port"),
      hardTerminationDeadline = config.getDuration("hard-termination-deadline").toScala
    )
}
