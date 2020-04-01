package bunyod.profunctors.routes.admin

import bunyod.profunctors.domain.brands.brands.BrandParam
import bunyod.profunctors.domain.brands.Brands
import bunyod.profunctors.domain.users.users.AdminUser
import bunyod.profunctors.effects.MonadThrow
import cats.Defer
import org.http4s.AuthedRoutes
import org.http4s.circe.JsonDecoder
import org.http4s.dsl.Http4sDsl
import org.http4s._
import bunyod.profunctors.routes.http.decoder._
import bunyod.profunctors.routes.http.json._
import org.http4s.server.{AuthMiddleware, Router}

final class AdminBrandRouter[F[_]: Defer: JsonDecoder: MonadThrow](
  brands: Brands[F]
) extends Http4sDsl[F] {

  private[admin] val pathPrefix = "/brands"
  private val httpRoutes: AuthedRoutes[AdminUser, F] =
    AuthedRoutes.of {
      case ar @ POST -> Root as _ =>
        ar.req.decodeR[BrandParam](bp => Created(brands.create(bp.toDomain)))
    }

  def routes(authMiddleware: AuthMiddleware[F, AdminUser]): HttpRoutes[F] = Router(
    pathPrefix -> authMiddleware(httpRoutes)
  )

}
