package bunyod.fp.domain.tokens

import dev.profunktor.auth.jwt.JwtToken

trait TokensAlgebra[F[_]] {

  def create: F[JwtToken]

}
