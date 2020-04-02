package bunyod.profunctors.routes.auth

import bunyod.profunctors.domain.auth.AuthPayloads.{InvalidUserOrPassword, LoginUser}
import cats.implicits._
import bunyod.profunctors.domain.auth.AuthAlgebra
import bunyod.profunctors.effects.MonadThrow
import bunyod.profunctors.routes.http.decoder._
import bunyod.profunctors.routes.http.json._
import cats.Defer
import org.http4s.HttpRoutes
import org.http4s.circe.JsonDecoder
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router

final class LoginRoutes[F[_]: Defer: JsonDecoder: MonadThrow](
  auth: AuthAlgebra[F]
) extends Http4sDsl[F] {

  private[auth] val pathPrefix = "/auth"

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ POST -> Root / "login" =>
      req.decodeR[LoginUser] { user =>
        auth
          .login(user.username.toDomain, user.password.toDomain)
          .flatMap(Ok(_))
          .recoverWith {
            case InvalidUserOrPassword(_) => Forbidden()
          }
      }
  }

  def routes: HttpRoutes[F] = Router(
    pathPrefix -> httpRoutes
  )
}
