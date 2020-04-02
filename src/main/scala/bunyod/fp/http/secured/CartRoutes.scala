package bunyod.fp.http.secured

import bunyod.fp.domain.cart._
import bunyod.fp.domain.cart.CartPayloads.Cart
import bunyod.fp.domain.items.ItemsPayloads.ItemId
import bunyod.fp.domain.users.UsersPayloads.CommonUser
import bunyod.fp.http.utils.json._
import cats._
import cats.implicits._
import org.http4s._
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
      case ar @ POST -> Root as user =>
        ar.req.asJsonDecode[Cart].flatMap { cart =>
          cart.items
            .map {
              case (id, quantity) =>
                shoppingCart.add(user.value.id, id, quantity)
            }
            .toList
            .sequence *> Created()
        }
      case DELETE -> Root / UUIDVar(uuid) as user =>
        shoppingCart.removeItem(user.value.id, ItemId(uuid)) *> NoContent()
    }

  def routes(authMiddleware: AuthMiddleware[F, CommonUser]): HttpRoutes[F] = Router(
    pathPrefix -> authMiddleware(httpRoutes)
  )

}
