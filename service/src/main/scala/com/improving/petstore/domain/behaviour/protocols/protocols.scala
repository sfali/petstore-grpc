package com.improving
package petstore
package domain
package behaviour
package protocols

import com.improving.petstore.api.scala_api.model.Pet
import enumeratum.values.{IntEnum, IntEnumEntry}
import enumeratum.{Enum, EnumEntry}
import org.apache.pekko.Done
import org.apache.pekko.actor.typed.ActorRef
import org.apache.pekko.pattern.StatusReply

sealed trait CborSerializable

sealed trait ErrorType extends EnumEntry with CborSerializable

object ErrorType extends Enum[ErrorType] {
  override def values: IndexedSeq[ErrorType] = findValues

  final case object InvalidArgument extends ErrorType
}

final case class ProcessingError(errorType: ErrorType, messages: Seq[String] = Seq.empty) extends Exception {

  override def getMessage: String = messages.mkString(System.lineSeparator())
}

sealed trait PetSerializable extends CborSerializable

// Pet state and its supporting classes
final case class PetState(
  id: Long,
  name: String,
  status: PetStatus,
  category: Option[Category] = None,
  imageIds: Set[String] = Set.empty,
  tags: Set[Tag] = Set.empty)
    extends PetSerializable

final case class Category(id: Long, name: String) extends PetSerializable

final case class Tag(id: Long, name: String) extends PetSerializable

sealed abstract class PetStatus(val value: Int) extends IntEnumEntry with PetSerializable

object PetStatus extends IntEnum[PetStatus] {

  final case object Unknown extends PetStatus(-1)
  final case object Available extends PetStatus(0)
  final case object Pending extends PetStatus(1)
  final case object Sold extends PetStatus(2)

  override lazy val values: IndexedSeq[PetStatus] = findValues
}

// Pet commands

sealed trait PetStoreCommandOrQueryEnvelope extends CborSerializable {
  val entityId: String
}

sealed trait PetCommandOrQueryEnvelope extends PetStoreCommandOrQueryEnvelope {
  val command: PetCommand

  override lazy val entityId: String = String.valueOf(command.petId)
}

sealed trait PetCommand extends CborSerializable {
  val petId: Long
}

final case class AddPet(pet: Pet) extends PetCommand {
  override val petId: Long = pet.id
}

final case class PetCommandEnvelope(command: PetCommand, replyTo: ActorRef[StatusReply[Done]])
    extends PetCommandOrQueryEnvelope
