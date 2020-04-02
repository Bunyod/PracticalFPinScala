package bunyod.fp.domain.items

import bunyod.fp.domain.brands.BrandsPayloads.BrandName
import bunyod.fp.domain.items.ItemsPayloads.{CreateItem, Item, ItemId, UpdateItem}

trait ItemsAlgebra[F[_]] {

  def findAll: F[List[Item]]
  def findBy(brand: BrandName): F[List[Item]]
  def findById(itemId: ItemId): F[Option[Item]]
  def create(item: CreateItem): F[Unit]
  def update(item: UpdateItem): F[Unit]
}
