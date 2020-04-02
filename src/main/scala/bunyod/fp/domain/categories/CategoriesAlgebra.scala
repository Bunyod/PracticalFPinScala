package bunyod.fp.domain.categories

import bunyod.fp.domain.categories.CategoryPayloads.{Category, CategoryName}

trait CategoriesAlgebra[F[_]] {

  def findAll: F[List[Category]]
  def create(category: CategoryName): F[Unit]

}
