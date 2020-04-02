package bunyod.profunctors.routes.auth

import bunyod.profunctors.domain.auth._
import bunyod.profunctors.domain.auth.AuthPayloads.{CreateUser, UserNameInUse}
import bunyod.profunctors.effects.MonadThrow
import cats._
import cats.implicits._
import org.http4s.HttpRoutes
import org.http4s.circe.JsonDecoder
import org.http4s.dsl.Http4sDsl
import bunyod.profunctors.routes.http.decoder._
import bunyod.profunctors.routes.http.json._
import org.http4s.server.Router

final class UserRoutes[F[_]: Defer: JsonDecoder: MonadThrow](auth: AuthAlgebra[F]) extends Http4sDsl[F] {

  private[auth] val pathPrefix = "/auth"

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ POST -> Root / "users" =>
      req.decodeR[CreateUser] { user =>
        auth
          .newUser(user.username.toDomain, user.password.toDomain)
          .flatMap(Created(_))
          .recoverWith {
            case UserNameInUse(u) => Conflict(u.value)
          }
      }
  }

  def routes: HttpRoutes[F] = Router {
    pathPrefix -> httpRoutes
  }

}
