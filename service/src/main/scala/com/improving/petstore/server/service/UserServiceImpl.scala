package com.improving
package petstore
package server
package service

import petstore.api.scala_api.UserService
import petstore.api.scala_api.model.{ApiResponse, CreateUsersRequest, UpdateUser, User, UserRequest}

import scala.concurrent.Future

class UserServiceImpl extends UserService {

  override def createUsersWithListInput(in: CreateUsersRequest): Future[ApiResponse] =
    Future.failed(unimplemented("createUsersWithListInput"))

  override def getUser(in: UserRequest): Future[User] = Future.failed(unimplemented("getUser"))

  override def updateUser(in: UpdateUser): Future[ApiResponse] = Future.failed(unimplemented("updateUser"))

  override def deleteUser(in: UserRequest): Future[User] = Future.failed(unimplemented("deleteUser"))

  override def createUser(in: User): Future[User] = Future.failed(unimplemented("createUser"))
}
