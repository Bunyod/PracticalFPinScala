package bunyod.profunctors.domain.auth

import bunyod.profunctors.domain.auth.AuthPayloads.{Password, UserName}
import dev.profunktor.auth.jwt.JwtToken

class AuthService[F[_]](authAlgebra: AuthAlgebra[F]) {

  def newUser(username: UserName, password: Password): F[JwtToken] =
    authAlgebra.newUser(username, password)

  def login(username: UserName, password: Password): F[JwtToken] =
    authAlgebra.login(username, password)

  def logout(token: JwtToken, username: UserName): F[Unit] =
    authAlgebra.logout(token, username)

}
