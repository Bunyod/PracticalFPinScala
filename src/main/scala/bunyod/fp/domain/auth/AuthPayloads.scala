package bunyod.fp.domain.auth

import eu.timepit.refined.types.string.NonEmptyString
import io.circe.Decoder
import io.estatico.newtype.macros.newtype
import java.util.UUID
import javax.crypto.Cipher
import scala.util.control.NoStackTrace

object AuthPayloads {

  @newtype case class UserId(value: UUID)
  @newtype case class UserName(value: String)
  @newtype case class Password(value: String)

  @newtype case class EncryptedPassword(value: String)

  @newtype case class EncryptCipher(value: Cipher)
  @newtype case class DecryptCipher(value: Cipher)

  @newtype case class UserNameParam(value: NonEmptyString) {
    def toDomain: UserName = UserName(value.value.toLowerCase())
  }

  @newtype case class PasswordParam(value: NonEmptyString) {
    def toDomain: Password = Password(value.value)
  }

  case class CreateUser(
    username: UserNameParam,
    password: PasswordParam
  )

  case class UserNameInUse(username: UserName) extends NoStackTrace
  case class InvalidUserOrPassword(username: UserName) extends NoStackTrace
  case object UnsupportedOperation extends NoStackTrace

  case object TokenNotFound extends NoStackTrace

  // ----- user login ------------

  case class LoginUser(
    username: UserNameParam,
    password: PasswordParam
  )

  // ----- admin auth ------------

  @newtype case class ClaimContent(claim: String)

  object ClaimContent {
    implicit val jsonDecoder: Decoder[ClaimContent] =
      Decoder.forProduct1("claim")(ClaimContent.apply)
  }

}
