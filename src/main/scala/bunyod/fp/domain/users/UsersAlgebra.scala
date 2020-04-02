package bunyod.fp.domain.users

import bunyod.fp.domain.auth.AuthPayloads.{Password, UserId, UserName}
import bunyod.fp.domain.users.UsersPayloads.User

trait UsersAlgebra[F[_]] {

  def find(username: UserName, password: Password): F[Option[User]]

  def create(username: UserName, password: Password): F[UserId]

}
