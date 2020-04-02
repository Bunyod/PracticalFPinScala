package bunyod.profunctors.routes.admin

import bunyod.profunctors.domain.categories.CategoriesAlgebra
import bunyod.profunctors.domain.categories.CategoryPayloads.CategoryParam
import bunyod.profunctors.domain.users.UsersPayloads.AdminUser
import bunyod.profunctors.effects.MonadThrow
import cats.Defer
import org.http4s.{AuthedRoutes, HttpRoutes}
import org.http4s.circe.JsonDecoder
import org.http4s.dsl.Http4sDsl
import bunyod.profunctors.routes.http.decoder._
import bunyod.profunctors.routes.http.json._
import org.http4s.server.{AuthMiddleware, Router}

final class AdminCategoryRoutes[F[_]: Defer: JsonDecoder: MonadThrow](categories: CategoriesAlgebra[F]) extends Http4sDsl[F] {

  private[admin] val pathPrefix = "/categories"

  private val httpRoutes: AuthedRoutes[AdminUser, F] =
    AuthedRoutes.of {
      case ar @ POST -> Root as _ =>
        ar.req.decodeR[CategoryParam](c => Created(categories.create(c.toDomain)))
    }

  def routes(authMiddleware: AuthMiddleware[F, AdminUser]): HttpRoutes[F] = Router(
    pathPrefix -> authMiddleware(httpRoutes)
  )
}
