package bunyod.profunctors.domain.categories

import bunyod.profunctors.domain.categories.CategoryPayloads.{Category, CategoryName}

class CategoriesService[F[_]](categoriesRepo: CategoriesAlgebra[F]) {

  def findAll: F[List[Category]] =
    categoriesRepo.findAll

  def create(name: CategoryName): F[Unit] =
    categoriesRepo.create(name)

}
