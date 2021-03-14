package bunyod.fp.http.admin

import bunyod.fp.domain.brands.BrandsPayloads.BrandParam
import bunyod.fp.domain.brands.BrandsService
import bunyod.fp.domain.users.UsersPayloads.AdminUser
import bunyod.fp.effekts.MonadThrow
import cats.Defer
import org.http4s.AuthedRoutes
import org.http4s.circe.JsonDecoder
import org.http4s.dsl.Http4sDsl
import org.http4s._
import bunyod.fp.http.utils.decoder._
import bunyod.fp.http.utils.json._
import org.http4s.server.{AuthMiddleware, Router}

final class AdminBrandRoutes[F[_]: Defer: JsonDecoder: MonadThrow](
  brands: BrandsService[F]
) extends Http4sDsl[F] {

  private[admin] val pathPrefix = "/brands"

  private val httpRoutes: AuthedRoutes[AdminUser, F] =
    AuthedRoutes.of { case ar @ POST -> Root as _ =>
      ar.req.decodeR[BrandParam](bp => Created(brands.create(bp.toDomain)))
    }

  def routes(authMiddleware: AuthMiddleware[F, AdminUser]): HttpRoutes[F] = Router(
    pathPrefix -> authMiddleware(httpRoutes)
  )

}
