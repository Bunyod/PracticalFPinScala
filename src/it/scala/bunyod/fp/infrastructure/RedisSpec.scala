package bunyod.fp.infrastructure

import bunyod.fp.domain.auth.AuthPayloads._
import bunyod.fp.domain.brands.BrandsPayloads._
import bunyod.fp.domain.cart.CartPayloads._
import bunyod.fp.domain.categories.CategoryPayloads._
import bunyod.fp.domain.items.ItemsAlgebra
import bunyod.fp.domain.items.ItemsPayloads.{CreateItem, Item, ItemId, UpdateItem}
import bunyod.fp.domain.tokens.TokenSyncService
import bunyod.fp.domain.users.UsersAlgebra
import bunyod.fp.domain.users.UsersPayloads.{User, UserJwtAuth}
import bunyod.fp.effekts.GenUUID
import bunyod.fp.infrastructure.redis._
import bunyod.fp.itsuite._
import bunyod.fp.suite.Arbitraries._
import bunyod.fp.utils.cfg.Configuration._
import cats.Eq
import cats.effect._
import bunyod.fp.logger.LoggerSuite._
import cats.effect.concurrent.Ref
import cats.implicits.{catsSyntaxEq => _, _}
import dev.profunktor.auth.jwt._
import dev.profunktor.redis4cats.{Redis, RedisCommands}
import dev.profunktor.redis4cats.log4cats._
import eu.timepit.refined.predicates.all.NonEmpty
import eu.timepit.refined.refineMV
import pdi.jwt._

import java.util.UUID
import scala.concurrent.duration._
import io.estatico.newtype.Coercible

class RedisScpec extends ResourceSuite[RedisCommands[IO, String, String]] {

  // For it:tests, one test is enough
  val MaxTests = MinSuccessful(1)

  override def resources =
    Redis[IO].utf8("redis://localhost")

  lazy val Exp         = ShoppingCartCfg(30.seconds)
  lazy val Secret      = refineMV[NonEmpty]("bar")
  lazy val tokenConfig = UserJwtCfg(Secret)
  lazy val tokenExp    = TokenExpirationCfg(30.seconds)
  lazy val jwtClaim    = JwtClaim("test")
  lazy val userJwtAuth = UserJwtAuth(JwtAuth.hmac("bar", JwtAlgorithm.HS256))
  lazy val tokenService = new TokenSyncService[IO](tokenConfig, tokenExp.value)

  withResources { cmd =>
    test("Shopping Cart") {
      forAll(MaxTests) { (uid: UserId, it1: Item, it2: Item, q1: Quantity, q2: Quantity) =>
        IOAssertion {
          Ref.of[IO, Map[ItemId, Item]](Map(it1.uuid -> it1, it2.uuid -> it2)).flatMap { ref =>
            val items = new TestItems(ref)
            LiveShoppingCart.make[IO](items, cmd, Exp).flatMap { c =>
              for {
                x <- c.get(uid)
                _ <- c.add(uid, it1.uuid, q1)
                _ <- c.add(uid, it2.uuid, q1)
                y <- c.get(uid)
                _ <- c.removeItem(uid, it1.uuid)
                z <- c.get(uid)
                _ <- c.update(uid, Cart(Map(it2.uuid -> q2)))
                w <- c.get(uid)
                _ <- c.delete(uid)
                v <- c.get(uid)
              } yield assert(
                x.items.isEmpty && y.items.size === 2 &&
                  z.items.size === 1 && v.items.isEmpty &&
                  w.items.headOption.fold(false)(_.quantity === q2)
              )
            }
          }
        }
      }
    }

    test("Authentication") {
      forAll(MaxTests) { (un1: UserName, un2: UserName, pw: Password) =>
        IOAssertion {
          for {
            a <- LiveAuthRepository.make(tokenExp, tokenService, new TestUsers(un2), cmd)
            u <- LiveUserAuthRepository.make[IO](cmd)
            x <- u.findUser(JwtToken("invalid"))(jwtClaim)
            j <- a.newUser(un1, pw)
            e <- jwtDecode[IO](j, userJwtAuth.value).attempt
            k <- a.login(un2, pw)
            f <- jwtDecode[IO](k, userJwtAuth.value).attempt
            _ <- a.logout(k, un2)
            y <- u.findUser(k)(jwtClaim)
            w <- u.findUser(j)(jwtClaim)
          } yield assert(
            x.isEmpty && e.isRight && f.isRight && y.isEmpty &&
              w.fold(false)(_.value.name === un1)
          )
        }
      }
    }
  }

}

protected class TestUsers(un: UserName) extends UsersAlgebra[IO] {
  /** If we have an Eq instance for Repr type R, derive an Eq instance for newtype N. */
  implicit def coercibleEq[R, N](implicit ev: Coercible[Eq[R], Eq[N]], R: Eq[R]): Eq[N] =
    ev(R)

  def find(username: UserName, password: Password): IO[Option[User]] =
    Eq[UserName].eqv(username, un).guard[Option].as(User(UserId(UUID.randomUUID), un)).pure[IO]
  def create(username: UserName, password: Password): IO[UserId] =
    GenUUID[IO].make[UserId]
}

protected class TestItems(ref: Ref[IO, Map[ItemId, Item]]) extends ItemsAlgebra[IO] {
  def findAll: IO[List[Item]] =
    ref.get.map(_.values.toList)
  def findBy(brand: BrandName): IO[List[Item]] =
    ref.get.map(_.values.filter(_.brand.name == brand).toList)
  def findById(itemId: ItemId): IO[Option[Item]] =
    ref.get.map(_.get(itemId))
  def create(item: CreateItem): IO[Unit] =
    GenUUID[IO].make[ItemId].flatMap { id =>
      val brand    = Brand(item.brandId, BrandName("foo"))
      val category = Category(item.categoryId, CategoryName("foo"))
      val newItem  = Item(id, item.name, item.description, item.price, brand, category)
      ref.update(_.updated(id, newItem))
    }
  def update(item: UpdateItem): IO[Unit] =
    ref.update(x => x.get(item.id).fold(x)(i => x.updated(item.id, i.copy(price = item.price))))
}
