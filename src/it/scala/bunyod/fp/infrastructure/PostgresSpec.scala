package bunyod.fp.infrastructure

import cats.effect._
import cats.implicits.{catsSyntaxEq => _, _}
import eu.timepit.refined.refineMV
import eu.timepit.refined.types.string.NonEmptyString
import io.estatico.newtype.ops._
import natchez.Trace.Implicits.noop
import bunyod.fp.suite.Arbitraries._
import bunyod.fp.domain.auth.AuthPayloads._
import bunyod.fp.domain.brands.BrandsPayloads._
import bunyod.fp.domain.categories.CategoryPayloads._
import bunyod.fp.domain.cart.CartPayloads._
import bunyod.fp.domain.crypto.LiveCrypto
import bunyod.fp.domain.items.ItemsPayloads._
import bunyod.fp.domain.orders.OrdersPayloads._
import bunyod.fp.infrastructure.postgres._
import bunyod.fp.itsuite._
import bunyod.fp.suite.PureTestSuite
import bunyod.fp.utils.cfg.Configuration.PasswordSaltCfg
import eu.timepit.refined.predicates.all.NonEmpty
import skunk._
import squants.market.Money

class PostgresSpec extends ResourceSuite[Resource[IO, Session[IO]]] with PureTestSuite {
  val MaxTests: PropertyCheckConfigParam = MinSuccessful(1)

  private lazy val secret: NonEmptyString = refineMV[NonEmpty]("53kr3t")

  lazy val salt = PasswordSaltCfg(secret)

  override def resources =
    Session.pooled[IO](
      host = "localhost",
      port = 5432,
      user = "postgres",
      database = "store",
      max = 10
    )

  withResources { pool =>
    test("Brands") {
      forAll(MaxTests) { (brand: Brand) =>
        IO {
          for {
            b <- LiveBrands.make[IO](pool)
            x <- b.findAll
            _ <- b.create(brand.name)
            y <- b.findAll
            z <- b.create(brand.name).attempt
          } yield assert(
            x.isEmpty && y.count(_.name === brand.name) === 1 && z.isLeft
          )
        }.unsafeRunSync()
      }
    }

    test("Categories") {
      forAll(MaxTests) { (category: Category) =>
        IOAssertion {
          for {
            c <- LiveCategories.make[IO](pool)
            x <- c.findAll
            _ <- c.create(category.name)
            y <- c.findAll
            z <- c.create(category.name).attempt
          } yield assert(
            x.isEmpty && y.count(_.name === category.name) === 1 && z.isLeft
          )
        }
      }
    }

    test("Items") {
      forAll(MaxTests) { (item: Item) =>
        def newItem(bid: Option[BrandId], cid: Option[CategoryId]): CreateItem = CreateItem(
          name = item.name,
          description = item.description,
          price = item.price,
          brandId = bid.getOrElse(item.brand.uuid),
          categoryId = cid.getOrElse(item.category.uuid)
        )

        IOAssertion {
          for {
            b <- LiveBrands.make[IO](pool)
            c <- LiveCategories.make[IO](pool)
            i <- LiveItems.make[IO](pool)
            x <- i.findAll
            _ <- b.create(item.brand.name)
            d <- b.findAll.map(_.headOption.map(_.uuid))
            _ <- c.create(item.category.name)
            e <- c.findAll.map(_.headOption.map(_.uuid))
            _ <- i.create(newItem(d, e))
            y <- i.findAll
          } yield assert(
            x.isEmpty && y.count(_.name === item.name) === 1
          )
        }
      }
    }

    test("Users") {
      forAll(MaxTests) { (username: UserName, password: Password) =>
        IOAssertion {
          for {
            c <- LiveCrypto.make[IO](salt)
            u <- LiveUsersRepository.make[IO](pool, c)
            d <- u.create(username, password)
            x <- u.find(username, password)
            y <- u.find(username, "foo".coerce[Password])
            z <- u.create(username, password).attempt
          } yield assert(
            x.count(_.id === d) === 1 && y.isEmpty && z.isLeft
          )
        }
      }
    }

    test("Orders") {
      forAll(MaxTests) {
        (oid: OrderId, pid: PaymentId, un: UserName, pw: Password, items: List[CartItem], price: Money) =>
          IOAssertion {
            for {
              o <- LiveOrderRepository.make[IO](pool)
              c <- LiveCrypto.make[IO](salt)
              u <- LiveUsersRepository.make[IO](pool, c)
              d <- u.create(un, pw)
              x <- o.findBy(d)
              y <- o.get(d, oid)
              i <- o.create(d, pid, items, price)
            } yield assert(
              x.isEmpty && y.isEmpty && i.value.version === 4
            )
          }
      }
    }
  }
}