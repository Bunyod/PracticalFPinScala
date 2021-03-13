package bunyod.fp.domain.users

import bunyod.fp.domain.auth.AuthPayloads.{UserId, UserName}
import io.estatico.newtype.macros.newtype
import dev.profunktor.auth.jwt.JwtSymmetricAuth
import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive

object UsersPayloads {

  @newtype case class AdminJwtAuth(value: JwtSymmetricAuth)
  @newtype case class UserJwtAuth(value: JwtSymmetricAuth)

  @derive(decoder, encoder)
  case class User(id: UserId, name: UserName)

  @newtype case class CommonUser(value: User)
  @newtype case class AdminUser(value: User)

}
