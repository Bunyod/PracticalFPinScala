package bunyod.fp.domain.users

import bunyod.fp.domain.auth.AuthPayloads.{Password, UserId, UserName}
import bunyod.fp.domain.users.UsersPayloads.User

class UsersService[F[_]](usersRepo: UsersAlgebra[F]) {

  def find(username: UserName, password: Password): F[Option[User]] =
    usersRepo.find(username, password)

  def create(username: UserName, password: Password): F[UserId] =
    usersRepo.create(username, password)

}
