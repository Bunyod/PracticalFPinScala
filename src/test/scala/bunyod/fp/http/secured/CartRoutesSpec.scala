package bunyod.fp.http.secured

import bunyod.fp.domain.auth.AuthPayloads
import bunyod.fp.domain.auth.AuthPayloads._
import bunyod.fp.domain.cart.CartPayloads._
import bunyod.fp.domain.cart._
import bunyod.fp.domain.items.ItemsPayloads.ItemId
import bunyod.fp.domain.users.UsersPayloads._
import bunyod.fp.suite.Generators._
import bunyod.fp.suite.HttpTestSuite
import cats.data.Kleisli
import cats.effect._
import org.http4s.Method._
import org.http4s._
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.client.dsl.io._
import org.http4s.server.AuthMiddleware
import squants.market.USD

import java.util.UUID

class CartRoutesSpec extends HttpTestSuite {

  val authUser = CommonUser(User(UserId(UUID.randomUUID()), UserName("testuser")))

  val authMiddleware: AuthMiddleware[IO, CommonUser] =
    AuthMiddleware(Kleisli.pure(authUser))

  def dataCart(cartTotal: CartTotal): ShoppingCartService[IO] = new ShoppingCartService[IO](
    new TestShoppingCart {
      override def get(userId: UserId): IO[CartTotal] =
        IO.pure(cartTotal)
    }
  )

  test("GET shopping cart [OK]") {
    forall(cartTotalGen) { cartTotal: CartTotal =>
      GET(Uri.unsafeFromString("/cart")).flatMap { req =>
        val routes = new CartRoutes[IO](dataCart(cartTotal)).routes(authMiddleware)
        assertHttp(routes, req)(Status.Ok, cartTotal)
      }
    }
  }

  test("POST add item to shopping cart [OK]") {
    forall(cartGen) { cart: Cart =>
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
