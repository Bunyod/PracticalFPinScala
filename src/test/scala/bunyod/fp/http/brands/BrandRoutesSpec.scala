package bunyod.fp.http.brands

import bunyod.fp.domain.brands.BrandsPayloads._
import bunyod.fp.domain.brands._
import bunyod.fp.suite.Arbitraries._
import bunyod.fp.http.utils.json._
import bunyod.fp.suite.HttpTestSuite
import cats.effect._
import cats.implicits._
import org.http4s.Method._
import org.http4s._
import org.http4s.client.dsl.io._

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

  forAll { b: List[Brand] =>
    spec("GET brands [OK]") {
      GET(Uri.unsafeFromString("/brands")).flatMap { req =>
        val routes = new BrandRoutes[IO](dataBrands(b)).routes
        assertHttp(routes, req)(Status.Ok, b)
      }
    }
  }

  forAll { b: List[Brand] =>
    spec("GET BRANDS[ERROR]") {
      GET(Uri.unsafeFromString("/brands")).flatMap { req =>
        val routes = new BrandRoutes[IO](failingBrands(b)).routes
        assertHttpFailure(routes, req)
      }
    }
  }
}

protected class TestBrandsService extends BrandsAlgebra[IO] {
  def create(name: BrandName): IO[Unit] = IO.unit
  def findAll: IO[List[Brand]] = IO.pure(List.empty)
}
