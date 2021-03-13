package bunyod.fp.http.items

import bunyod.fp.domain.brands.BrandsPayloads.BrandName
import bunyod.fp.domain.items.ItemsPayloads._
import bunyod.fp.domain.items._
import bunyod.fp.suite.Generators._
import bunyod.fp.suite.HttpTestSuite
import cats.effect._
import cats.implicits._
import org.http4s.Method._
import org.http4s._
import org.http4s.client.dsl.io._
import org.scalacheck.Gen

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
    forall(Gen.listOf(itemGen)) { i: List[Item] =>
      GET(Uri.unsafeFromString("/items")).flatMap { req =>
        val routes = new ItemRoutes[IO](dataItems(i)).routes
        assertHttp(routes, req)(Status.Ok, i)
      }
    }
  }

  test("GET items by brand succeeds") {
    val gen = for {
      i <- Gen.listOf(itemGen)
      b <- brandGen
    } yield i -> b

    forall(gen) { case (it, b) =>
      GET(Uri.uri("/items").withQueryParam(b.name.value)).flatMap { req =>
        val routes = new ItemRoutes[IO](dataItems(it)).routes
        assertHttp(routes, req)(Status.Ok, it)
      }
    }
  }

  test("GET items [ERROR]") {
    forall(Gen.listOf(itemGen)) { i: List[Item] =>
      GET(Uri.unsafeFromString("/items")).flatMap { req =>
        val routes = new ItemRoutes[IO](failingItems(i)).routes
        assertHttpFailure(routes, req)
      }
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
