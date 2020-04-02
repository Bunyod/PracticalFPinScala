package bunyod.profunctors.domain.users

import bunyod.profunctors.domain.auth.AuthPayloads.{UserId, UserName}
import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder}
import io.estatico.newtype.macros.newtype
import bunyod.profunctors.routes.http.json._

object UsersPayloads {

  case class User(id: UserId, name: UserName)

  @newtype case class CommonUser(value: User)
  @newtype case class AdminUser(value: User)

  implicit val userDecoder: Decoder[User] = deriveDecoder[User]
  implicit val userEncoder: Encoder[User] = deriveEncoder[User]

}
