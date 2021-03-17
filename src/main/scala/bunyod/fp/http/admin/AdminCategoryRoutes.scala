package bunyod.fp.http.admin

import bunyod.fp.domain.categories.CategoriesService
import bunyod.fp.domain.categories.CategoryPayloads.CategoryParam
import bunyod.fp.domain.users.UsersPayloads.AdminUser
import cats.Defer
import cats.effect._
import org.http4s.{AuthedRoutes, HttpRoutes}
import org.http4s.circe.JsonDecoder
import org.http4s.dsl.Http4sDsl
import bunyod.fp.http.utils.decoder._
import bunyod.fp.http.utils.json._
import org.http4s.server.{AuthMiddleware, Router}

final class AdminCategoryRoutes[F[_]: Defer: JsonDecoder: MonadThrow](categories: CategoriesService[F])
  extends Http4sDsl[F] {

  private[admin] val pathPrefix = "/categories"

  private val httpRoutes: AuthedRoutes[AdminUser, F] =
    AuthedRoutes.of { case ar @ POST -> Root as _ =>
      ar.req.decodeR[CategoryParam](c => Created(categories.create(c.toDomain)))
    }

  def routes(authMiddleware: AuthMiddleware[F, AdminUser]): HttpRoutes[F] = Router(
    pathPrefix -> authMiddleware(httpRoutes)
  )
}
