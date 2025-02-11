package com.improving
package petstore

import com.google.rpc.{Code, Status}
import io.grpc.StatusRuntimeException
import io.grpc.protobuf.StatusProto

package object server {

  def unimplemented(name: String): StatusRuntimeException =
    StatusProto.toStatusRuntimeException(
      Status
        .newBuilder()
        .setCode(Code.UNIMPLEMENTED_VALUE)
        .setMessage(s"Function `$name` is not yet implemented!")
        .build()
    )

  def invalidArgument(message: String): StatusRuntimeException =
    StatusProto.toStatusRuntimeException(
      Status
        .newBuilder()
        .setCode(Code.INVALID_ARGUMENT_VALUE)
        .setMessage(message)
        .build()
    )

  def internalError(message: Option[String] = None): StatusRuntimeException =
    StatusProto.toStatusRuntimeException(
      Status
        .newBuilder()
        .setCode(Code.INTERNAL_VALUE)
        .setMessage(message.getOrElse("Unknown internal error occurred"))
        .build()
    )
}
