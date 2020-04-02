package bunyod.profunctors.infrastructure.redis

import bunyod.profunctors.domain.auth.AuthPayloads._
import bunyod.profunctors.domain.auth._
import bunyod.profunctors.domain.tokens.TokensAlgebra
import bunyod.profunctors.domain.users.UsersAlgebra
import bunyod.profunctors.domain.users.UsersPayloads.User
import bunyod.profunctors.effects._
import cats.effect.Sync
import dev.profunktor.auth.jwt.JwtToken
import dev.profunktor.redis4cats.algebra.RedisCommands
import cats.implicits._
import io.circe.syntax._

object LiveAuthInterpreter {

  def make[F[_]: Sync](
    tokenExpiration: TokenExpiration,
    tokens: TokensAlgebra[F],
    users: UsersAlgebra[F],
    redis: RedisCommands[F, String, String]
  ): F[AuthAlgebra[F]] =
    Sync[F].delay(
      new LiveAuthInterpreter[F](tokenExpiration, tokens, users, redis)
    )

}

final class LiveAuthInterpreter[F[_]: GenUUID: MonadThrow] private (
  tokenExpiration: TokenExpiration,
  tokens: TokensAlgebra[F],
  users: UsersAlgebra[F],
  redis: RedisCommands[F, String, String]
) extends AuthAlgebra[F] {

  private val TokenExpiration = tokenExpiration.value

  override def newUser(username: UserName, password: Password): F[JwtToken] =
    users.find(username, password).flatMap {
      case Some(_) => UserNameInUse(username).raiseError[F, JwtToken]
      case None =>
        for {
          i <- users.create(username, password)
          t <- tokens.create
          u = User(i, username).asJson.noSpaces
          _ <- redis.setEx(t.value, u, TokenExpiration)
          _ <- redis.setEx(username.value, t.value, TokenExpiration)
        } yield t
    }

  override def login(username: UserName, password: Password): F[JwtToken] =
    users.find(username, password).flatMap {
      case None => InvalidUserOrPassword(username).raiseError[F, JwtToken]
      case Some(user) =>
        redis.get(username.value).flatMap {
          case Some(t) => JwtToken(t).pure[F]
          case None =>
            tokens.create.flatTap { t =>
              redis.setEx(t.value, user.asJson.noSpaces, TokenExpiration) *>
                redis.setEx(username.value, t.value, TokenExpiration)

            }
        }
    }

  override def logout(token: JwtToken, username: UserName): F[Unit] =
    redis.del(token.value) *> redis.del(username.value)

}
