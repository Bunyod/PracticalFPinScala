package bunyod.fp.http.items

import bunyod.fp.domain.brands.BrandsPayloads.BrandName
import bunyod.fp.domain.items.ItemsPayloads._
import bunyod.fp.domain.items._
import bunyod.fp.http.utils.json._
import bunyod.fp.suite.Arbitraries._
import bunyod.fp.suite.HttpTestSuite
import cats.effect._
import cats.implicits._
import org.http4s._

class ItemRoutesSpec extends HttpTestSuite {

  def dataItems(items: List[Item]): ItemsService[IO] = new ItemsService[IO](
    new TestItems {
      override def findAll: IO[List[Item]] =
        IO.pure(items)
    }
  )

  def failingItems(items: List[Item]): ItemsService[IO] = new ItemsService[IO](
    new TestItems {
      override def findAll: IO[List[Item]] =
        IO.raiseError(DummyError) *> IO.pure(items)

      override def findBy(brand: BrandName): IO[List[Item]] =
        findAll
    }
  )

  test("GET items [OK]") {
    forAll { i: List[Item] =>
      val request = Request[IO](method = Method.GET, uri = Uri.unsafeFromString("/items"))
      val routes = new ItemRoutes[IO](dataItems(i)).routes
      assertHttp(routes, request)(Status.Ok, i)
    }
  }

  test("GET items [ERROR]") {
    forAll { i: List[Item] =>
      val request = Request[IO](method = Method.GET, uri = Uri.unsafeFromString("/items"))
      val routes = new ItemRoutes[IO](failingItems(i)).routes
      assertHttpFailure(routes, request)
    }
  }

}

protected class TestItems extends ItemsAlgebra[IO] {

  override def findAll: IO[List[Item]] = IO.pure(List.empty)
  override def findBy(brand: BrandName): IO[List[Item]] = IO.pure(List.empty)
  override def findById(itemId: ItemId): IO[Option[Item]] = IO.pure(none[Item])
  override def create(item: ItemsPayloads.CreateItem): IO[Unit] = IO.unit
  override def update(item: ItemsPayloads.UpdateItem): IO[Unit] = IO.unit

}
