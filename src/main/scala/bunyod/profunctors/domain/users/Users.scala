package bunyod.profunctors.domain.users
import bunyod.profunctors.domain.auth.auth.{Password, UserId, UserName}
import bunyod.profunctors.domain.users.users.User

trait Users[F[_]] {

  def find(
    username: UserName,
    password: Password
  ): F[Option[User]]

  def create(
    username: UserName,
    password: Password
  ): F[UserId]

}
