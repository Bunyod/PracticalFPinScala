package bunyod.fp.domain.users

import bunyod.fp.domain.auth.AuthPayloads.{UserId, UserName}
import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder}
import io.estatico.newtype.macros.newtype
import bunyod.fp.http.utils.json._
import dev.profunktor.auth.jwt.JwtSymmetricAuth

object UsersPayloads {

  @newtype case class AdminJwtAuth(value: JwtSymmetricAuth)
  @newtype case class UserJwtAuth(value: JwtSymmetricAuth)

  case class User(id: UserId, name: UserName)

  @newtype case class CommonUser(value: User)
  @newtype case class AdminUser(value: User)

  implicit val userDecoder: Decoder[User] = deriveDecoder[User]
  implicit val userEncoder: Encoder[User] = deriveEncoder[User]

}
