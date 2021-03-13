package bunyod.fp.domain.brands

import java.util.UUID
import scala.util.control.NoStackTrace

import bunyod.fp.effekts.uuid
import bunyod.fp.http.utils.http4s.queryParam
import bunyod.fp.http.utils.refined._

import derevo.cats._
import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import eu.timepit.refined.types.string.NonEmptyString
import io.circe.Decoder
import io.circe.refined._
import io.estatico.newtype.macros.newtype

object BrandsPayloads {

  @derive(decoder, encoder, eqv, show, uuid)
  @newtype case class BrandId(value: UUID)

  @derive(decoder, encoder, eqv, show)
  @newtype case class BrandName(value: String) {
    def toBrand(brandId: BrandId): Brand =
      Brand(brandId, this)
  }

  @derive(queryParam)
  @newtype case class BrandParam(value: NonEmptyString) {
    def toDomain: BrandName = BrandName(value.value.toLowerCase.capitalize)
  }
  object BrandParam {
    implicit val jsonDecoder: Decoder[BrandParam] =
      Decoder.forProduct1("name")(BrandParam.apply)
  }

  @derive(decoder, encoder, eqv, show)
  case class Brand(uuid: BrandId, name: BrandName)

  @derive(decoder, encoder)
  case class InvalidBrand(value: String) extends NoStackTrace

}
