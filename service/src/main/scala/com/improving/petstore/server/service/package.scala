package com.improving
package petstore
package server

import com.improving.petstore.domain.behaviour.pet.PetServiceEntity
import com.improving.petstore.domain.behaviour.protocols.PetCommandOrQueryEnvelope
import org.apache.pekko.cluster.sharding.typed.scaladsl.{ClusterSharding, EntityRef}

package object service {

  def getPetEntity(sharding: ClusterSharding, petId: Long): EntityRef[PetCommandOrQueryEnvelope] =
    sharding.entityRefFor(PetServiceEntity.EntityKey, petId.toString)
}
