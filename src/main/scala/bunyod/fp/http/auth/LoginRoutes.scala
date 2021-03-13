package bunyod.fp.http.auth

import bunyod.fp.domain.auth._
import bunyod.fp.domain.auth.AuthPayloads._
import bunyod.fp.effects.MonadThrow
import bunyod.fp.http.utils.decoder._
import bunyod.fp.http.utils.json._
import cats.Defer
import cats.implicits._
import org.http4s.HttpRoutes
import org.http4s.circe.JsonDecoder
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router

final class LoginRoutes[F[_]: Defer: JsonDecoder: MonadThrow](
  auth: AuthService[F]
) extends Http4sDsl[F] {

  private[auth] val pathPrefix = "/auth"

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] { case req @ POST -> Root / "login" =>
    req.decodeR[LoginUser] { user =>
      auth
        .login(user.username.toDomain, user.password.toDomain)
        .flatMap(Ok(_))
        .recoverWith { case InvalidUserOrPassword(_) =>
          Forbidden()
        }
    }
  }

  def routes: HttpRoutes[F] = Router(
    pathPrefix -> httpRoutes
  )
}
