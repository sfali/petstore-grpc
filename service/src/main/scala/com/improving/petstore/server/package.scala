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
}
