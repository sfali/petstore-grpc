package com.improving
package petstore
package pekko
package gateway

import com.improving.grpc_rest_gateway.runtime.server.GatewayServer
import petstore.api.scala_api.{OrderServiceGatewayHandler, PetServiceGatewayHandler, UserServiceGatewayHandler}
import com.typesafe.config.Config
import org.apache.pekko
import pekko.actor.NoSerializationVerificationNeeded
import pekko.actor.typed.scaladsl.Behaviors
import pekko.actor.typed.{ActorSystem, Behavior}
import pekko.grpc.GrpcClientSettings
import pekko.management.scaladsl.PekkoManagement

import scala.util.{Failure, Success}

object Main {

  def main(args: Array[String]): Unit = {
    val systemName = "PetstorePekkoGateway"
    if (args.nonEmpty && args(0) == "local")
      ActorSystem[Nothing](
        GuardianBehavior(),
        systemName,
        utils.LocalConfig(
          serviceName = "petstore-pekko-gateway",
          nodeNum = 1,
          totalNumOfNodes = 1,
          remotePort = 18355,
          managementPort = 9559,
          nettyImpl = false
        )
      )
    else ActorSystem[Nothing](GuardianBehavior(), systemName)
  }
}

object GuardianBehavior {

  private sealed trait Command extends NoSerializationVerificationNeeded
  private case object Init extends Command
  private case object FailedInit extends Command
  private case object SystemStarted extends Command

  def apply(): Behavior[Nothing] =
    Behaviors
      .setup[Command] { context =>
        context.log.info("staring actor system: {}", context.self.path.name)

        implicit val system: ActorSystem[Nothing] = context.system
        val config = system.settings.config

        context.self ! Init

        Behaviors.receiveMessagePartial {
          case Init =>
            context.pipeToSelf(PekkoManagement(system).start()) {
              case Failure(ex) =>
                context.log.error("Unable to start system", ex)
                FailedInit

              case Success(_) => SystemStarted
            }
            Behaviors.same

          case FailedInit =>
            context.log.error("Unable to start service.")
            system.terminate()
            Behaviors.stopped

          case SystemStarted =>
            val grpcClientSettings = GrpcClientSettings.fromConfig("pekko-gateway")
            startGatewayServer(config.getConfig("rest-gateway"), grpcClientSettings)

            Behaviors.same
        }
      }
      .narrow

  private def startGatewayServer(
    config: Config,
    grpcClientSettings: GrpcClientSettings
  )(implicit
    system: ActorSystem[?]
  ): Unit =
    GatewayServer(
      config = config,
      handlers = PetServiceGatewayHandler(grpcClientSettings),
      OrderServiceGatewayHandler(grpcClientSettings),
      UserServiceGatewayHandler(grpcClientSettings)
    ).run()
}
