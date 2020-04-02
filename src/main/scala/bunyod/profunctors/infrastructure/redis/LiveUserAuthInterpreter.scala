package bunyod.profunctors.infrastructure.redis

import bunyod.profunctors.domain.auth.UserAuthAlgebra
import bunyod.profunctors.domain.users.UsersPayloads._
import cats.Functor
import cats.effect.Sync
import cats.implicits._
import dev.profunktor.auth.jwt
import dev.profunktor.redis4cats.algebra.RedisCommands
import io.circe.parser.decode
import pdi.jwt.JwtClaim

object LiveUserAuthInterpreter {

  def make[F[_]: Sync](
    redis: RedisCommands[F, String, String]
  ): F[UserAuthAlgebra[F, CommonUser]] =
    Sync[F].delay(
      new LiveUserAuthInterpreter(redis)
    )

}

class LiveUserAuthInterpreter[F[_]: Functor](
  redis: RedisCommands[F, String, String]
) extends UserAuthAlgebra[F, CommonUser] {

  override def findUser(jwtToken: jwt.JwtToken)(
    claim: JwtClaim
  ): F[Option[CommonUser]] =
    redis
      .get(jwtToken.value)
      .map(_.flatMap(u => decode[User](u).toOption.map(CommonUser.apply)))

}
