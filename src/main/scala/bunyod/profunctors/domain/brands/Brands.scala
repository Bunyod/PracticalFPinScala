package bunyod.profunctors.domain.brands

import bunyod.profunctors.domain.brands.brands.{Brand, BrandName}

trait Brands[F[_]] {

  def findAll: F[List[Brand]]
  def create(brand: BrandName): F[Unit]
}
