package bunyod.fp.domain.brands

import bunyod.fp.domain.brands.BrandsPayloads.{Brand, BrandName}

trait BrandsAlgebra[F[_]] {

  def findAll: F[List[Brand]]
  def create(brand: BrandName): F[Unit]
}
