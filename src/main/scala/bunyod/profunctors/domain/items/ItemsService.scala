package bunyod.profunctors.domain.items

import bunyod.profunctors.domain.brands.BrandsPayloads.BrandName
import bunyod.profunctors.domain.items.ItemsPayloads.{CreateItem, Item, ItemId, UpdateItem}

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
