package bunyod.fp.http.brands

import bunyod.fp.domain.brands.BrandsPayloads._
import bunyod.fp.domain.brands._
import bunyod.fp.http.utils.json._
import bunyod.fp.suite.Arbitraries._
import bunyod.fp.suite.HttpTestSuite
import cats.effect._
import org.http4s._

class BrandRoutesSpec extends HttpTestSuite {

  def dataBrands(brands: List[Brand]): BrandsService[IO] =
    new BrandsService[IO](new TestBrandsService {
      override def findAll: IO[List[Brand]] =
        IO.pure(brands)
    })

  def failingBrands(brands: List[Brand]): BrandsService[IO] =
    new BrandsService[IO](new TestBrandsService {
      override def findAll: IO[List[Brand]] =
        IO.raiseError(DummyError) *> IO.pure(brands)

    })

  test("GET brands [OK]") {
    forAll { (b: List[Brand]) =>
      val request = Request[IO](method = Method.GET, uri = Uri.unsafeFromString("/brands"))
      val routes = new BrandRoutes[IO](dataBrands(b)).routes
      assertHttp(routes, request)(Status.Ok, b)
    }
  }

  test("GET BRANDS[ERROR]") {
    forAll { b: List[Brand] =>
      val request = Request[IO](method = Method.GET, uri = Uri.unsafeFromString("/brands"))
      val routes = new BrandRoutes[IO](failingBrands(b)).routes
      assertHttpFailure(routes, request)
    }
  }
}

protected class TestBrandsService extends BrandsAlgebra[IO] {
  def create(name: BrandName): IO[Unit] = IO.unit
  def findAll: IO[List[Brand]] = IO.pure(List.empty)
}
