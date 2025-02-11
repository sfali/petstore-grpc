package com.improving
package petstore
package domain
package behaviour

import protocols.{Category, PetState, PetStatus, Tag}
import petstore.api.scala_api.model.{Pet, Category as ProtoCategory, PetStatus as ProtoPetStatus, Tag as ProroTag}

package object pet {

  implicit class PetStateOps(src: PetState) {
    def merge(pet: Pet): PetState =
      src.copy(
        name = nonEmptyString(src.name, pet.name),
        status = pet.status.merge(src.status),
        category = nonEmptyCategory(src.category, pet.category),
        tags = src.tags ++ pet.tags.map(_.toTag)
      )
  }

  implicit class PetStatusOps(src: PetStatus) {
    def toPetStatus: ProtoPetStatus =
      src match {
        case PetStatus.Unknown   => ProtoPetStatus.pending
        case PetStatus.Available => ProtoPetStatus.available
        case PetStatus.Pending   => ProtoPetStatus.pending
        case PetStatus.Sold      => ProtoPetStatus.sold
      }
  }

  implicit class ProtoPetStatusOps(src: ProtoPetStatus) {
    def toPetStatus: PetStatus =
      src match {
        case ProtoPetStatus.available => PetStatus.Available
        case ProtoPetStatus.pending   => PetStatus.Pending
        case ProtoPetStatus.sold      => PetStatus.Sold
        case _                        => PetStatus.Unknown
      }

    def merge(current: PetStatus): PetStatus = {
      val target = toPetStatus
      (current, target) match {
        case (_, PetStatus.Unknown) => current
        case _                      => target
      }
    }
  }

  implicit class ProtoTagOps(src: ProroTag) {
    def toTag: Tag = Tag(id = src.id, name = src.name)
  }

  private def nonEmptyString(src: String, target: String) =
    (src, target) match {
      case (_, t) if !t.isBlank => t
      case (s, t) if t.isBlank  => s
    }

  private def nonEmptyCategory(src: Option[Category], target: Option[ProtoCategory]) =
    target.map(c => Category(c.id, c.name)).orElse(src)

}
