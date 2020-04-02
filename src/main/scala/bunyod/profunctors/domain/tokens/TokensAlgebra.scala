package bunyod.profunctors.domain.tokens

import dev.profunktor.auth.jwt.JwtToken

trait TokensAlgebra[F[_]] {

  def create: F[JwtToken]

}
