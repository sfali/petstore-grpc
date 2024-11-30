package com.improving
package petstore
package server

import petstore.api.scala_api.{OrderService, OrderServiceHandler, PetService, PetServiceHandler, UserService, UserServiceHandler}
import server.service.{OrderServiceImpl, PetServiceImpl, UserServiceImpl}
import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.grpc.scaladsl.{ServerReflection, ServiceHandler}
import org.apache.pekko.http.scaladsl.Http

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class GrpcServer(settings: HttpSettings)(implicit system: ActorSystem[?]) {
  private implicit val ec: ExecutionContext = system.executionContext

  def run(): Future[Http.ServerBinding] = {
    val services = ServiceHandler.concatOrNotFound(
      PetServiceHandler.partial(new PetServiceImpl()),
      OrderServiceHandler.partial(new OrderServiceImpl()),
      UserServiceHandler.partial(new UserServiceImpl()),
      ServerReflection.partial(List(PetService, OrderService, UserService))
    )
    val bound: Future[Http.ServerBinding] =
      Http()
        .newServerAt(settings.host, settings.port)
        .bind(services)
        .map(
          _.addToCoordinatedShutdown(hardTerminationDeadline = settings.hardTerminationDeadline)
        )

    bound.onComplete {
      case Success(binding) =>
        val address = binding.localAddress
        system
          .log
          .info(
            s"gRPC server bound to {}:{}",
            address.getHostString,
            address.getPort
          )
      case Failure(ex) => system.log.error("Failed to bind gRPC endpoint, terminating system", ex)
    }
    bound
  }
}

object GrpcServer {
  def apply(settings: HttpSettings)(implicit system: ActorSystem[?]): GrpcServer =
    new GrpcServer(settings)
}
