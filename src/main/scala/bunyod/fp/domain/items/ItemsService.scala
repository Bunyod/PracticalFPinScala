package bunyod.fp.domain.items

import bunyod.fp.domain.brands.BrandsPayloads.BrandName
import bunyod.fp.domain.items.ItemsPayloads.{CreateItem, Item, ItemId, UpdateItem}

class ItemsService[F[_]](itemsRepo: ItemsAlgebra[F]) {

  def findAll: F[List[Item]] =
    itemsRepo.findAll

  def findBy(brand: BrandName): F[List[Item]] =
    itemsRepo.findBy(brand)

  def findById(itemId: ItemId): F[Option[Item]] =
    itemsRepo.findById(itemId)

  def create(item: CreateItem): F[Unit] =
    itemsRepo.create(item)

  def update(item: UpdateItem): F[Unit] =
    itemsRepo.update(item)

}
