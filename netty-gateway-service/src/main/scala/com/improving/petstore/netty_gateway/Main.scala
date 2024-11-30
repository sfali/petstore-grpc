package com.improving
package petstore
package netty_gateway

import com.improving.grpc_rest_gateway.runtime.server.GatewayServer
import petstore.api.scala_api.{OrderServiceGatewayHandler, PetServiceGatewayHandler, UserServiceGatewayHandler}
import com.typesafe.config.Config
import org.apache.pekko.Done
import org.apache.pekko.actor.{CoordinatedShutdown, NoSerializationVerificationNeeded}
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.actor.typed.{ActorSystem, Behavior}
import org.apache.pekko.management.scaladsl.PekkoManagement

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

object Main {

  def main(args: Array[String]): Unit = {
    val systemName = "PetstoreNettyRestGateway"
    if (args.nonEmpty && args(0) == "local")
      ActorSystem[Nothing](
        GuardianBehavior(),
        systemName,
        utils.LocalConfig(
          serviceName = "petstore-netty-gateway",
          nodeNum = 1,
          totalNumOfNodes = 1,
          remotePort = 18355,
          managementPort = 9559
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
        implicit val ec: ExecutionContext = system.executionContext
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
            startGatewayServer(config.getConfig("rest-gateway"))
            Behaviors.same
        }
      }
      .narrow

  private def startGatewayServer(config: Config)(implicit system: ActorSystem[?], ex: ExecutionContext): Unit = {
    val gatewayServer =
      GatewayServer(
        config = config,
        toHandlers = channel =>
          Seq(PetServiceGatewayHandler(channel), OrderServiceGatewayHandler(channel), UserServiceGatewayHandler(channel)),
        executor = None
      )

    CoordinatedShutdown(system)
      .addTask(CoordinatedShutdown.PhaseServiceStop, "gateway-server-stop") { () =>
        Try(gatewayServer.stop())
        Future.successful(Done)
      }
    gatewayServer.start()
  }
}
