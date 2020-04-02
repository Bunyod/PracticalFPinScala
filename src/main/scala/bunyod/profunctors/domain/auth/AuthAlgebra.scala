package bunyod.profunctors.domain.auth

import bunyod.profunctors.domain.auth.AuthPayloads.{Password, UserName}
import dev.profunktor.auth.jwt.JwtToken

trait AuthAlgebra[F[_]] {

  def newUser(username: UserName, password: Password): F[JwtToken]
  def login(username: UserName, password: Password): F[JwtToken]
  def logout(token: JwtToken, username: UserName): F[Unit]

}
