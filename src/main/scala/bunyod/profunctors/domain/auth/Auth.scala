package bunyod.profunctors.domain.auth

import bunyod.profunctors.domain.auth.auth.{Password, UserName}
import bunyod.profunctors.domain.users.users.User
import dev.profunktor.auth.jwt.JwtToken

trait Auth[F[_]] {

  def findUser(token: JwtToken): F[Option[User]]
  def newUser(username: UserName, password: Password): F[JwtToken]
  def login(username: UserName, password: Password): F[JwtToken]
  def logout(token: JwtToken, username: UserName): F[Unit]

}
