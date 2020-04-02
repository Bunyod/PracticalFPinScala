package bunyod.profunctors.domain.brands

import bunyod.profunctors.domain.brands.BrandsPayloads.{Brand, BrandName}

class BrandsService[F[_]](brandsRepo: BrandsAlgebra[F]) {

    def findAll: F[List[Brand]] =
        brandsRepo.findAll

    def create(brand: BrandName): F[Unit] =
        brandsRepo.create(brand)


}
