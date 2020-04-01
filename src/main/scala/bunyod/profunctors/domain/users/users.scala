package bunyod.profunctors.domain.users

import bunyod.profunctors.domain.auth.auth.{UserId, UserName}
import io.estatico.newtype.macros.newtype

object users {
  case class User(id: UserId, name: UserName)

  @newtype case class CommonUser(value: User)
  @newtype case class AdminUser(value: User)

}
