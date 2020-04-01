package bunyod.profunctors.domain.categories

import bunyod.profunctors.domain.categories.CategoryPayloads.{Category, CategoryName}

trait Categories[F[_]] {

  def findAll: F[List[Category]]
  def create(category: CategoryName): F[Unit]

}
