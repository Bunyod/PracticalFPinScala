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
import org.http4s.server.AuthMiddleware
import squants.market.USD

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
    forAll { cartTotal: CartTotal =>
      val request = Request[IO](method = Method.GET, uri = Uri.unsafeFromString("/cart"))
      val routes = new CartRoutes[IO](dataCart(cartTotal)).routes(authMiddleware)
      assertHttp(routes, request)(Status.Ok, cartTotal)
    }
  }

  test("POST add item to shopping cart [OK]") {
    forAll { cart: Cart =>
      val cartTotal = CartTotal(List.empty, USD(0))
      val routes = new CartRoutes[IO](dataCart(cartTotal)).routes(authMiddleware)
      val request = Request[IO](method = Method.POST, uri = Uri.unsafeFromString("/cart")).withEntity(cart)
      assertHttpStatus(routes, request)(Status.Created)
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
