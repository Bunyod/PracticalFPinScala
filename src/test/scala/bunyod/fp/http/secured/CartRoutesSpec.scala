package bunyod.fp.http.secured

import bunyod.fp.domain.auth.AuthPayloads
import bunyod.fp.domain.auth.AuthPayloads._
import bunyod.fp.domain.cart.CartPayloads._
import bunyod.fp.domain.cart._
import bunyod.fp.domain.items.ItemsPayloads.ItemId
import bunyod.fp.domain.users.UsersPayloads._
import bunyod.fp.http.utils.json._
import bunyod.fp.suite.Arbitraries._
import bunyod.fp.suite.HttpTestSuite
import cats.data.Kleisli
import cats.effect.IO
import java.util.UUID
import org.http4s._
import org.http4s.Method._
import org.http4s.client.dsl.io._
import org.http4s.server.AuthMiddleware
import squants.market.USD

class CartRoutesSpec extends HttpTestSuite {

  val authUser = CommonUser(User(UserId(UUID.randomUUID()), UserName("testuser")))

  val authMiddleware: AuthMiddleware[IO, CommonUser] =
    AuthMiddleware(Kleisli.pure(authUser))

  def dataCart(cartTotal: CartTotal): ShoppingCartService[IO] =
    new ShoppingCartService[IO](
      new TestShoppingCart {
        override def get(userId: UserId): IO[CartTotal] =
          IO.pure(cartTotal)
      }
    )

  forAll { cartTotal: CartTotal =>
    spec("GET shopping cart [OK]") {
      GET(Uri.unsafeFromString("/cart")).flatMap { req =>
        val routes = new CartRoutes[IO](dataCart(cartTotal)).routes(authMiddleware)
        assertHttp(routes, req)(Status.Ok, cartTotal)
      }
    }
  }

  forAll { cart: Cart =>
    spec("POST add item to shopping cart [OK]") {
      POST(cart, Uri.unsafeFromString("/cart")).flatMap { req =>
        val cartTotal = CartTotal(List.empty, USD(0))
        val routes = new CartRoutes[IO](dataCart(cartTotal)).routes(authMiddleware)
        assertHttpStatus(routes, req)(Status.Created)

      }
    }
  }
}

protected class TestShoppingCart extends ShoppingCartAlgebra[IO] {

  override def add(userId: UserId, itemId: ItemId, quantity: Quantity): IO[Unit] = IO.unit
  override def delete(userId: AuthPayloads.UserId): IO[Unit] = IO.unit
  override def get(userId: UserId): IO[CartTotal] = IO.pure(CartTotal(List.empty, USD(0d)))
  override def removeItem(userId: UserId, itemId: ItemId): IO[Unit] = IO.unit
  override def update(userId: UserId, cart: Cart): IO[Unit] = IO.unit

}
