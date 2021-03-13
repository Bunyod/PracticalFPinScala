package bunyod.fp.domain
package items

import java.util.UUID
import bunyod.fp.domain.brands.BrandsPayloads.{Brand, BrandId}
import bunyod.fp.domain.categories.CategoryPayloads.{Category, CategoryId}
import bunyod.fp.effekts.uuid

import derevo.cats._
import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import eu.timepit.refined.api.Refined
import eu.timepit.refined.string.{Uuid, ValidBigDecimal}
import eu.timepit.refined.types.string.NonEmptyString
import io.circe.refined._
import io.circe.{KeyDecoder, KeyEncoder}
import io.estatico.newtype.macros.newtype
import squants.market._

object ItemsPayloads {

  @derive(decoder, encoder, eqv, show, uuid)
  @newtype case class ItemId(value: UUID)
  object ItemId {
    implicit val keyEncoder: KeyEncoder[ItemId] = deriving
    implicit val keyDecoder: KeyDecoder[ItemId] = deriving
  }

  @derive(decoder, encoder, eqv, show)
  @newtype case class ItemName(value: String)

  @derive(decoder, encoder, eqv, show)
  @newtype case class ItemDescription(value: String)

  @derive(decoder, encoder, eqv, show)
  case class Item(
    uuid: ItemId,
    name: ItemName,
    description: ItemDescription,
    price: Money,
    brand: Brand,
    category: Category
  )

  // ----- Create item -------------------

  @derive(decoder, encoder)
  @newtype case class ItemNameParam(value: NonEmptyString)

  @derive(decoder, encoder)
  @newtype case class ItemDescriptionParam(value: NonEmptyString)

  @derive(decoder, encoder)
  @newtype case class PriceParam(value: String Refined ValidBigDecimal)

  @derive(decoder, encoder)
  case class CreateItemParam(
    name: ItemNameParam,
    description: ItemDescriptionParam,
    price: PriceParam,
    brandId: BrandId,
    categoryId: CategoryId
  ) {

    def toDomain: CreateItem =
      CreateItem(
        ItemName(name.value.value),
        ItemDescription(description.value.value),
        USD(BigDecimal(price.value.value)),
        brandId,
        categoryId
      )
  }

  case class CreateItem(
    name: ItemName,
    description: ItemDescription,
    price: Money,
    brandId: BrandId,
    categoryId: CategoryId
  )

  // ----- Update item -------------------

  @derive(decoder, encoder)
  @newtype case class ItemIdParam(value: String Refined Uuid)

  @derive(decoder, encoder)
  case class UpdateItemParam(
    id: ItemIdParam,
    price: PriceParam
  ) {

    def toDomain: UpdateItem =
      UpdateItem(
        ItemId(UUID.fromString(id.value.value)),
        USD(BigDecimal(price.value.value))
      )
  }

  @derive(decoder, encoder)
  case class UpdateItem(
    id: ItemId,
    price: Money
  )

}
