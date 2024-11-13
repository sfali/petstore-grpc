package com.improving
package petstore
package server
package service

import petstore.api.scala_api.PetService
import petstore.api.scala_api.model.{
  ApiResponse,
  FindByStatusRequest,
  Pet,
  PetByIdRequest,
  UpdatePetRequest,
  UploadImageRequest
}

import scala.concurrent.Future

class PetServiceImpl extends PetService {

  override def uploadImage(in: UploadImageRequest): Future[ApiResponse] = Future.failed(unimplemented("uploadImage"))

  override def addPet(in: Pet): Future[ApiResponse] = Future.failed(unimplemented("addPet"))

  override def updatePet(in: Pet): Future[ApiResponse] = Future.failed(unimplemented("updatePet"))

  override def findByStatus(in: FindByStatusRequest): Future[Pet] = Future.failed(unimplemented("findByStatus"))

  override def findByPetId(in: PetByIdRequest): Future[Pet] = Future.failed(unimplemented("findByPetId"))

  override def updatePetById(in: UpdatePetRequest): Future[Pet] = Future.failed(unimplemented("updatePetById"))

  override def deleteByPetId(in: PetByIdRequest): Future[Pet] = Future.failed(unimplemented("updatePetById"))
}
