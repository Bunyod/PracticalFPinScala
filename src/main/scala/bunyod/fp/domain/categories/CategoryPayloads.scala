package bunyod.fp.domain.categories

import bunyod.fp.effekts.uuid
import derevo.cats._
import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import eu.timepit.refined.types.string.NonEmptyString
import io.circe.Decoder
import io.circe.refined._
import io.estatico.newtype.macros.newtype
import java.util.UUID

object CategoryPayloads {

  @derive(decoder, encoder, eqv, show, uuid)
  @newtype case class CategoryId(value: UUID)

  @derive(decoder, encoder, eqv, show)
  @newtype case class CategoryName(value: String)

  @newtype case class CategoryParam(value: NonEmptyString) {
    def toDomain: CategoryName = CategoryName(value.value.toLowerCase.capitalize)
  }

  object CategoryParam {
    implicit val jsonDecoder: Decoder[CategoryParam] =
      Decoder.forProduct1("name")(CategoryParam.apply)
  }

  @derive(decoder, encoder, eqv, show)
  case class Category(uuid: CategoryId, name: CategoryName)

}
