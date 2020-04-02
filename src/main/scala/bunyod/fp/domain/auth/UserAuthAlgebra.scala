package bunyod.fp.domain.auth

import dev.profunktor.auth.jwt.JwtToken
import pdi.jwt.JwtClaim

trait UserAuthAlgebra[F[_], A] {

  def findUser(jwtToken: JwtToken)(claim: JwtClaim): F[Option[A]]
}
