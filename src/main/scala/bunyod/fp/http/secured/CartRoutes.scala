package bunyod.fp.http.secured

import bunyod.fp.domain.cart._
import bunyod.fp.domain.cart.CartPayloads._
import bunyod.fp.domain.items.ItemsPayloads._
import bunyod.fp.domain.users.UsersPayloads.CommonUser

import cats._
import cats.syntax.all._
import org.http4s._
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.server._

final class CartRoutes[F[_]: Defer: JsonDecoder: Monad](
  shoppingCart: ShoppingCartService[F]
) extends Http4sDsl[F] {

  private[http] val pathPrefix = "/cart"

  private val httpRoutes: AuthedRoutes[CommonUser, F] =
    AuthedRoutes.of {
      case GET -> Root as user =>
        Ok(shoppingCart.get(user.value.id))
      // Add items to the cart
      case ar @ POST -> Root as user =>
        ar.req.asJsonDecode[Cart].flatMap { cart =>
          cart.items
            .map { case (id, quantity) =>
              shoppingCart.add(user.value.id, id, quantity)
            }
            .toList
            .sequence *> Created()
        }

      // Modify items in the cart
      case ar @ PUT -> Root as user =>
        ar.req.asJsonDecode[Cart].flatMap { cart =>
          shoppingCart.update(user.value.id, cart) *> Ok()
        }
      case DELETE -> Root / UUIDVar(uuid) as user =>
        shoppingCart.removeItem(user.value.id, ItemId(uuid)) *> NoContent()
    }

  def routes(authMiddleware: AuthMiddleware[F, CommonUser]): HttpRoutes[F] = Router(
    pathPrefix -> authMiddleware(httpRoutes)
  )

}
