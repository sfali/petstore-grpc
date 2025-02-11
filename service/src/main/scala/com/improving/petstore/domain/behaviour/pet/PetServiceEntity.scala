package com.improving
package petstore
package domain
package behaviour
package pet

import behaviour.protocols.ProcessingError
import behaviour.protocols.PetStatus.Unknown
import behaviour.protocols.{AddPet, ErrorType, PetCommandEnvelope, PetCommandOrQueryEnvelope, PetState}
import org.apache.pekko
import pekko.pattern.StatusReply
import pekko.actor.typed.{ActorRef, Behavior}
import pekko.actor.typed.scaladsl.{ActorContext, Behaviors}
import pekko.cluster.sharding.typed.scaladsl.{ClusterSharding, Entity, EntityTypeKey}
import pekko.persistence.typed.PersistenceId
import pekko.persistence.typed.state.scaladsl.{DurableStateBehavior, Effect, ReplyEffect}

object PetServiceEntity {

  val EntityKey: EntityTypeKey[PetCommandOrQueryEnvelope] =
    EntityTypeKey[PetCommandOrQueryEnvelope]("PetEntity")

  private def commandHandler(
    context: ActorContext[PetCommandOrQueryEnvelope]
  )(state: PetState,
    command: PetCommandOrQueryEnvelope
  ): ReplyEffect[PetState] =
    command match {
      case PetCommandEnvelope(AddPet(pet), replyTo) =>
        var messages = Seq.empty[String]
        if (pet.id <= 0) {
          messages = messages :+ "Pet id cannot be equal or less than 0."
        }
        if (state.name.isBlank && pet.name.isBlank) {
          messages = messages :+ "Pet name cannot be empty"
        }
        if (messages.nonEmpty) {
          Effect
            .none[PetState]
            .thenReply(replyTo)(_ => StatusReply.Error(ProcessingError(ErrorType.InvalidArgument, messages)))
        } else {
          Effect.persist(state.merge(pet)).thenReply(replyTo)(_ => StatusReply.Ack)
        }

      case PetCommandEnvelope(command, replyTo) =>
        context.log.warn("Unhandled command: {}", command.getClass.getName)
        Effect.none[PetState].thenReply(replyTo)(_ => StatusReply.Error("Command unhandled"))
    }

  def apply(id: Long): Behavior[PetCommandOrQueryEnvelope] =
    Behaviors.setup[PetCommandOrQueryEnvelope] { context =>
      context.log.debug("Initializing pet entity: {}", id)

      DurableStateBehavior
        .withEnforcedReplies[PetCommandOrQueryEnvelope, PetState](
          persistenceId = PersistenceId(EntityKey.name, String.valueOf(id)),
          emptyState = PetState(id = id, name = "", status = Unknown),
          commandHandler = commandHandler(context)
        )
    }

  def init(
    sharding: ClusterSharding,
    numberOfShards: Int = 30
  ): ActorRef[PetCommandOrQueryEnvelope] =
    sharding
      .init(
        Entity(typeKey = EntityKey)(
          createBehavior = context => PetServiceEntity(context.entityId.toLong)
        ).withMessageExtractor(PetstoreServiceMessageExtractor[PetCommandOrQueryEnvelope](numberOfShards))
      )
}
