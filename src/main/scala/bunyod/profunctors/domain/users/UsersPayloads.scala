package bunyod.profunctors.domain.users

import bunyod.profunctors.domain.auth.AuthPayloads.{UserId, UserName}
import io.estatico.newtype.macros.newtype

object UsersPayloads {
  case class User(id: UserId, name: UserName)

  @newtype case class CommonUser(value: User)
  @newtype case class AdminUser(value: User)

}
