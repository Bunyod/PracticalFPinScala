package bunyod.profunctors.routes.secured

import bunyod.profunctors.domain.orders.Orders
import bunyod.profunctors.domain.orders.orders.OrderId
import bunyod.profunctors.domain.users.users.CommonUser
import cats.{Defer, Monad}
import org.http4s.{AuthedRoutes, HttpRoutes}
import org.http4s.dsl.Http4sDsl
import org.http4s.server.{AuthMiddleware, Router}
import bunyod.profunctors.routes.http.json._

final class OrderRoutes[F[_]: Defer: Monad](
  orders: Orders[F]
) extends Http4sDsl[F] {

  private[routes] val pathPrefix = "/orders"

  private val httpRoutes: AuthedRoutes[CommonUser, F] = AuthedRoutes.of {

    case GET -> Root as user =>
      Ok(orders.findBy(user.value.id))

    case GET -> Root / UUIDVar(uuid) as user =>
      Ok(orders.get(user.value.id, OrderId(uuid)))

  }

  def routes(authMiddleware: AuthMiddleware[F, CommonUser]): HttpRoutes[F] = Router {
    pathPrefix -> authMiddleware(httpRoutes)
  }

}
