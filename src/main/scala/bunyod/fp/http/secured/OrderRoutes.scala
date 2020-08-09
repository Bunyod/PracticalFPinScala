package bunyod.fp.http.secured

import bunyod.fp.domain.orders._
import bunyod.fp.domain.orders.OrdersPayloads.OrderId
import bunyod.fp.domain.users.UsersPayloads.CommonUser
import bunyod.fp.http.utils.json._
import cats._
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.server._

final class OrderRoutes[F[_]: Defer: Monad](
  orders: OrdersService[F]
) extends Http4sDsl[F] {

  private[http] val pathPrefix = "/orders"

  private val httpRoutes: AuthedRoutes[CommonUser, F] = AuthedRoutes.of {

    case GET -> Root as user =>
      Ok(orders.findBy(user.value.id))

    case GET -> Root / UUIDVar(uuid) as user =>
      Ok(orders.get(user.value.id, OrderId(uuid)))

  }

  def routes(authMiddleware: AuthMiddleware[F, CommonUser]): HttpRoutes[F] =
    Router {
      pathPrefix -> authMiddleware(httpRoutes)
    }

}
