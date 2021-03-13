package bunyod.fp.http.auth

import bunyod.fp.domain.auth._
import bunyod.fp.domain.auth.AuthPayloads._
import bunyod.fp.effekts.MonadThrow
import cats._
import cats.implicits._
import org.http4s.HttpRoutes
import org.http4s.circe.JsonDecoder
import org.http4s.dsl.Http4sDsl
import bunyod.fp.http.utils.decoder._
import bunyod.fp.http.utils.params._
import org.http4s.server.Router

final class UserRoutes[F[_]: Defer: JsonDecoder: MonadThrow](
  auth: AuthService[F]
) extends Http4sDsl[F] {

  private[auth] val prefixPath = "/auth"

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] { case req @ POST -> Root / "users" =>
    req
      .decodeR[CreateUser] { user =>
        auth
          .newUser(user.username.toDomain, user.password.toDomain)
          .flatMap(Created(_))
          .recoverWith { case UserNameInUse(u) =>
            Conflict(u.value)
          }
      }

  }

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )

}
