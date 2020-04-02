package bunyod.profunctors.domain.users

import bunyod.profunctors.domain.auth.AuthPayloads.{Password, UserId, UserName}
import bunyod.profunctors.domain.users.UsersPayloads.User

trait UsersAlgebra[F[_]] {

  def find(username: UserName, password: Password): F[Option[User]]

  def create(username: UserName, password: Password): F[UserId]

}
