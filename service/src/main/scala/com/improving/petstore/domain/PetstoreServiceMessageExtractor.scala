package com.improving
package petstore
package domain

import petstore.domain.behaviour.protocols.PetStoreCommandOrQueryEnvelope
import org.apache.pekko.cluster.sharding.typed.HashCodeNoEnvelopeMessageExtractor

object PetstoreServiceMessageExtractor {

  def apply[C <: PetStoreCommandOrQueryEnvelope](
    numberOfShards: Int
  ): HashCodeNoEnvelopeMessageExtractor[C] =
    new HashCodeNoEnvelopeMessageExtractor[C](numberOfShards) {

      // $COVERAGE-OFF$
      override def entityId(message: C): String = {
        val id = message.entityId
        if (id.isBlank) {
          throw new IllegalArgumentException("Id cannot be empty")
        }
        id
      }
      // $COVERAGE-ON$
    }
}
