package bunyod.fp.domain.auth

import bunyod.fp.effekts.uuid

import java.util.UUID
import javax.crypto.Cipher
import scala.util.control.NoStackTrace
import derevo.cats._
import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import eu.timepit.refined.types.string.NonEmptyString
import io.circe._
import io.circe.refined._
import io.estatico.newtype.macros.newtype

object AuthPayloads {

  @derive(decoder, encoder, eqv, show, uuid)
  @newtype case class UserId(value: UUID)

  @derive(decoder, encoder, eqv, show)
  @newtype case class UserName(value: String)

  @derive(decoder, encoder, eqv, show)
  @newtype case class Password(value: String)

  @derive(decoder, encoder)
  @newtype case class EncryptedPassword(value: String)

  @newtype case class EncryptCipher(value: Cipher)
  @newtype case class DecryptCipher(value: Cipher)

  @derive(decoder, encoder)
  @newtype case class UserNameParam(value: NonEmptyString) {
    def toDomain: UserName = UserName(value.value.toLowerCase())
  }

  @derive(decoder, encoder)
  @newtype case class PasswordParam(value: NonEmptyString) {
    def toDomain: Password = Password(value.value)
  }

  @derive(decoder, encoder)
  case class CreateUser(
    username: UserNameParam,
    password: PasswordParam
  )

  case class UserNotFound(username: UserName) extends NoStackTrace
  case class UserNameInUse(username: UserName) extends NoStackTrace
  case class InvalidUserOrPassword(username: UserName) extends NoStackTrace
  case object UnsupportedOperation extends NoStackTrace
  case object TokenNotFound extends NoStackTrace

  // ----- user login ------------
  @derive(decoder, encoder)
  case class LoginUser(
    username: UserNameParam,
    password: PasswordParam
  )

  // ----- admin auth ------------

  @newtype case class ClaimContent(uuid: UUID)

  object ClaimContent {
    implicit val jsonDecoder: Decoder[ClaimContent] =
      Decoder.forProduct1("uuid")(ClaimContent.apply)
  }

}
