package bunyod.profunctors.routes.secured

import bunyod.profunctors.domain.cart.ShoppingCartAlgebra
import bunyod.profunctors.domain.cart.CartPayloads.Cart
import bunyod.profunctors.domain.items.ItemsPayloads.ItemId
import bunyod.profunctors.domain.users.UsersPayloads.CommonUser
import cats.implicits._
import cats.{Defer, Monad}
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.{AuthMiddleware, Router}
import org.http4s.{AuthedRoutes, HttpRoutes}
import bunyod.profunctors.routes.http.json._

final class CartRoutes[F[_]: Defer: JsonDecoder: Monad](shoppingCart: ShoppingCartAlgebra[F]) extends Http4sDsl[F] {

  private[routes] val pathPrefix = "/cart"

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
