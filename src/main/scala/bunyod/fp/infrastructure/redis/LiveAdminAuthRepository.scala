package bunyod.fp.infrastructure.redis

import bunyod.fp.domain.auth.UserAuthAlgebra
import bunyod.fp.domain.users.UsersPayloads.AdminUser
import cats.Applicative
import cats.effect.Sync
import dev.profunktor.auth.jwt.JwtToken
import pdi.jwt.JwtClaim
import cats.implicits._

object LiveAdminAuthRepository {

  def make[F[_]: Sync](
    adminToken: JwtToken,
    adminUser: AdminUser
  ): F[UserAuthAlgebra[F, AdminUser]] =
    Sync[F].delay(
      new LiveAdminAuthRepository(adminToken, adminUser)
    )
}

class LiveAdminAuthRepository[F[_]: Applicative](
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
