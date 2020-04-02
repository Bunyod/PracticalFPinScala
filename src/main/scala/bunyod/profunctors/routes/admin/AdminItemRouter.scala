package bunyod.profunctors.routes.admin

import bunyod.profunctors.domain.items._
import bunyod.profunctors.domain.items.ItemsPayloads.{CreateItemParam, UpdateItemParam}
import bunyod.profunctors.domain.users.UsersPayloads.AdminUser
import bunyod.profunctors.effects.MonadThrow
import cats.Defer
import org.http4s.{AuthedRoutes, HttpRoutes}
import org.http4s.circe.JsonDecoder
import org.http4s.dsl.Http4sDsl
import bunyod.profunctors.routes.http.decoder._
import bunyod.profunctors.routes.http.json._
import org.http4s.server.{AuthMiddleware, Router}

final class AdminItemRouter[F[_]: Defer: JsonDecoder: MonadThrow](
  items: ItemsAlgebra[F]
) extends Http4sDsl[F] {

  private[admin] val pathPrefix = "/items"

  private val httpRoutes: AuthedRoutes[AdminUser, F] =
    AuthedRoutes.of {
      case ar @ POST -> Root as _ =>
        ar.req.decodeR[CreateItemParam](item => Created(items.create(item.toDomain)))
      case ar @ PUT -> Root as _ =>
        ar.req.decodeR[UpdateItemParam](item => Ok(items.update(item.toDomain)))
    }

  def routes(authMiddleware: AuthMiddleware[F, AdminUser]): HttpRoutes[F] = Router(
    pathPrefix -> authMiddleware(httpRoutes)
  )
}
