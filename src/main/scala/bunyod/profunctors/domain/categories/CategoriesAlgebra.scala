package bunyod.profunctors.domain.categories

import bunyod.profunctors.domain.categories.CategoryPayloads.{Category, CategoryName}

trait CategoriesAlgebra[F[_]] {

  def findAll: F[List[Category]]
  def create(category: CategoryName): F[Unit]

}
