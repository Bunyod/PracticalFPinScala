package bunyod.fp.infrastructure.redis

import bunyod.fp.domain.auth.UserAuthAlgebra
import bunyod.fp.domain.users.UsersPayloads._
import cats.Functor
import cats.effect.Sync
import cats.implicits._
import dev.profunktor.auth.jwt
import dev.profunktor.redis4cats.algebra.RedisCommands
import io.circe.parser.decode
import pdi.jwt.JwtClaim

object LiveUserAuthRepository {

  def make[F[_]: Sync](
    redis: RedisCommands[F, String, String]
  ): F[UserAuthAlgebra[F, CommonUser]] =
    Sync[F].delay(
      new LiveUserAuthRepository(redis)
    )

}

class LiveUserAuthRepository[F[_]: Functor](
  redis: RedisCommands[F, String, String]
) extends UserAuthAlgebra[F, CommonUser] {

  override def findUser(jwtToken: jwt.JwtToken)(
    claim: JwtClaim
  ): F[Option[CommonUser]] =
    redis
      .get(jwtToken.value)
      .map(_.flatMap(u => decode[User](u).toOption.map(CommonUser.apply)))

}
