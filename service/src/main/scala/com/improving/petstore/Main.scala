package com.improving
package petstore

import petstore.server.{GrpcServer, HttpSettings}
import petstore.domain.behaviour.pet.PetServiceEntity
import com.typesafe.config.Config
import org.apache.pekko.Done
import org.apache.pekko.actor.NoSerializationVerificationNeeded
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.actor.typed.{ActorSystem, Behavior}
import org.apache.pekko.cluster.sharding.typed.scaladsl.ClusterSharding
import org.apache.pekko.management.cluster.bootstrap.ClusterBootstrap
import org.apache.pekko.management.scaladsl.PekkoManagement

import scala.util.{Failure, Success}

object Main {

  def main(args: Array[String]): Unit = {
    val systemName = "PetstoreGrpcRestGateway"
    if (args.nonEmpty && args(0) == "local")
      ActorSystem[Nothing](
        GuardianBehavior(),
        systemName,
        utils.LocalConfig(serviceName = "petstore-grpc-service", nodeNum = 1, totalNumOfNodes = 1)
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
            ClusterBootstrap(system).start()
            val sharding = ClusterSharding(system)
            PetServiceEntity.init(sharding)

            context.pipeToSelf(init(config.getConfig("app.grpc-settings"), sharding)) {
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

          case SystemStarted => Behaviors.same
        }
      }
      .narrow

  private def init(config: Config, sharding: ClusterSharding)(implicit system: ActorSystem[?]) = {
    import system.executionContext
    for {
      _ <- PekkoManagement(system).start()
      _ <- GrpcServer(HttpSettings(config), sharding).run()
    } yield Done
  }
}
