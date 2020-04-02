package bunyod.profunctors.infrastructure.redis

import bunyod.profunctors.domain.auth.UserAuthAlgebra
import bunyod.profunctors.domain.users.UsersPayloads.AdminUser
import cats.Applicative
import cats.effect.Sync
import dev.profunktor.auth.jwt.JwtToken
import pdi.jwt.JwtClaim
import cats.implicits._

object LiveAdminAuthInterpreter {

  def make[F[_]: Sync](
    adminToken: JwtToken,
    adminUser: AdminUser
  ): F[UserAuthAlgebra[F, AdminUser]] =
    Sync[F].delay(
      new LiveAdminAuthInterpreter(adminToken, adminUser)
    )
}

class LiveAdminAuthInterpreter[F[_]: Applicative](
  adminToken: JwtToken,
  adminUser: AdminUser
) extends UserAuthAlgebra[F, AdminUser] {

  override def findUser(token: JwtToken)(
    claim: JwtClaim
  ): F[Option[AdminUser]] =
    (token == adminToken)
      .guard[Option]
      .as(adminUser)
      .pure[F]
}
