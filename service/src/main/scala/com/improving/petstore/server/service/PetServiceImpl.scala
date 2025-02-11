package com.improving
package petstore
package server
package service

import com.typesafe.scalalogging.LazyLogging
import petstore.domain.behaviour.protocols.{AddPet, ErrorType, PetCommandEnvelope, ProcessingError}
import petstore.api.scala_api.model.{
  ApiResponse,
  FindByStatusRequest,
  Pet,
  PetByIdRequest,
  UpdatePetRequest,
  UploadImageRequest
}
import org.apache.pekko
import pekko.Done
import pekko.cluster.sharding.typed.scaladsl.ClusterSharding
import pekko.pattern.{AskTimeoutException, StatusReply}
import pekko.util.Timeout
import petstore.api.scala_api.PetService

import scala.concurrent.duration.*
import scala.concurrent.{ExecutionContext, Future}

class PetServiceImpl(sharding: ClusterSharding)(implicit ec: ExecutionContext) extends PetService with LazyLogging {

  private implicit val timeout: Timeout = Timeout(5.seconds)

  override def uploadImage(in: UploadImageRequest): Future[ApiResponse] = Future.failed(unimplemented("uploadImage"))

  override def addPet(in: Pet): Future[ApiResponse] = {
    val petId = in.id
    getPetEntity(sharding, petId)
      .ask[StatusReply[Done]](actorRef => PetCommandEnvelope(command = AddPet(in), replyTo = actorRef))
      .flatMap {
        case StatusReply.Ack => Future.successful(ApiResponse(code = 0, `type` = "OK", message = "Pet saved"))
        case StatusReply.Error(pe @ ProcessingError(errorType, _)) if ErrorType.InvalidArgument == errorType =>
          Future.failed(invalidArgument(pe.getMessage))
      }
      .recoverWith {
        case ex: AskTimeoutException =>
          logger.error(s"Ask time out for addPet operation for id: $petId", ex)
          Future.failed(internalError())
        case ex =>
          logger.error(s"Unhandled exception occurred for addPet operation for id: $petId", ex)
          Future.failed(internalError(Some(ex.getMessage)))
      }
  }

  override def updatePet(in: Pet): Future[ApiResponse] = Future.failed(unimplemented("updatePet"))

  override def findByStatus(in: FindByStatusRequest): Future[Pet] = Future.failed(unimplemented("findByStatus"))

  override def findByPetId(in: PetByIdRequest): Future[Pet] = Future.failed(unimplemented("findByPetId"))

  override def updatePetById(in: UpdatePetRequest): Future[Pet] = Future.failed(unimplemented("updatePetById"))

  override def deleteByPetId(in: PetByIdRequest): Future[Pet] = Future.failed(unimplemented("updatePetById"))

}
