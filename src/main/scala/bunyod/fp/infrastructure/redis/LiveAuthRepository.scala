package bunyod.fp.infrastructure.redis

import bunyod.fp.domain.auth.AuthPayloads._
import bunyod.fp.domain.auth._
import bunyod.fp.domain.tokens.TokensAlgebra
import bunyod.fp.domain.users.UsersAlgebra
import bunyod.fp.domain.users.UsersPayloads._
import bunyod.fp.utils.cfg.Configuration.TokenExpirationCfg
import cats.MonadThrow
import cats.effect._
import dev.profunktor.auth.jwt.JwtToken
import dev.profunktor.redis4cats.RedisCommands
import cats.implicits._
import io.circe.syntax._

object LiveAuthRepository {

  def make[F[_]: Sync](
    tokenExpiration: TokenExpirationCfg,
    tokens: TokensAlgebra[F],
    users: UsersAlgebra[F],
    redis: RedisCommands[F, String, String]
  ): F[AuthAlgebra[F]] =
    Sync[F].delay(
      new LiveAuthRepository[F](tokenExpiration, tokens, users, redis)
    )

}

final class LiveAuthRepository[F[_]: MonadThrow] private (
  tokenExpiration: TokenExpirationCfg,
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
    redis.del(token.value) *> redis.del(username.value).void

}
