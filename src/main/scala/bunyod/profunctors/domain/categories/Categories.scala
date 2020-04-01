package bunyod.profunctors.domain.categories

import bunyod.profunctors.domain.categories.categories.{Category, CategoryName}

trait Categories[F[_]] {

  def findAll: F[List[Category]]
  def create(category: CategoryName): F[Unit]

}
