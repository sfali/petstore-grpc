package com.improving
package petstore
package server
package service

import petstore.api.scala_api.OrderService
import petstore.api.scala_api.model.{ApiResponse, Order, OrderByIdRequest}

import scala.concurrent.Future

class OrderServiceImpl extends OrderService {

  override def placeOrder(in: Order): Future[Order] = Future.failed(unimplemented("placeOrder"))

  override def findOrderById(in: OrderByIdRequest): Future[Order] = Future.failed(unimplemented("findOrderById"))

  override def deleteOrderById(in: OrderByIdRequest): Future[ApiResponse] =
    Future.failed(unimplemented("deleteOrderById"))
}
