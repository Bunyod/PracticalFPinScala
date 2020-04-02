package bunyod.profunctors.domain.brands

import bunyod.profunctors.domain.brands.BrandsPayloads.{Brand, BrandName}

trait BrandsAlgebra[F[_]] {

  def findAll: F[List[Brand]]
  def create(brand: BrandName): F[Unit]
}
