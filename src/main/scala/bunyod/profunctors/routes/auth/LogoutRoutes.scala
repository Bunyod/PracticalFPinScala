package bunyod.profunctors.routes.auth

import bunyod.profunctors.domain.auth.Auth
import bunyod.profunctors.domain.users.UsersPayloads.CommonUser
import cats._
import cats.implicits._
import dev.profunktor.auth.AuthHeaders
import org.http4s.{AuthedRoutes, HttpRoutes}
import org.http4s.dsl.Http4sDsl
import org.http4s.server.{AuthMiddleware, Router}

final class LogoutRoutes[F[_]: Defer: Monad](
  auth: Auth[F]
) extends Http4sDsl[F] {

  private[auth] val pathPrefix = "/auth"

  private val httpRoutes: AuthedRoutes[CommonUser, F] = AuthedRoutes.of {
    case ar @ POST -> Root / "logout" as user =>
      AuthHeaders
        .getBearerToken[F](ar.req)
        .traverse_(t => auth.logout(t, user.value.name)) *> NoContent()
  }

  def routes(authMiddleware: AuthMiddleware[F, CommonUser]): HttpRoutes[F] = Router(
    pathPrefix -> authMiddleware(httpRoutes)
  )
}
