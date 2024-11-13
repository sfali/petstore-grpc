package com.improving
package petstore

import com.improving.grpc_rest_gateway.runtime.server.GatewayServer
import com.improving.petstore.api.scala_api.{
  OrderServiceGatewayHandler,
  PetServiceGatewayHandler,
  UserServiceGatewayHandler
}
import com.improving.petstore.server.{GrpcServer, HttpSettings}
import com.typesafe.config.Config
import org.apache.pekko.Done
import org.apache.pekko.actor.{CoordinatedShutdown, NoSerializationVerificationNeeded}
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.actor.typed.{ActorSystem, Behavior}
import org.apache.pekko.management.scaladsl.PekkoManagement

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

object Main {

  def main(args: Array[String]): Unit = {
    val systemName = "PetstoreGrpcRestGateway"
    if (args.nonEmpty && args(0) == "local")
      ActorSystem[Nothing](GuardianBehavior(), systemName, utils.LocalConfig(1, 1))
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
            context.pipeToSelf(init(config.getConfig("app.grpc-settings"))) {
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

  private def init(config: Config)(implicit system: ActorSystem[?]) = {
    import system.executionContext
    for {
      _ <- PekkoManagement(system).start()
      _ <- GrpcServer(HttpSettings(config)).run()
    } yield Done
  }

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
        gatewayServer.stop()
        Future.successful(Done)
      }
    gatewayServer.start()
  }
}
