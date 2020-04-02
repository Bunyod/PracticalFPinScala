package bunyod.profunctors.domain.crypto

import bunyod.profunctors.domain.auth.AuthPayloads._
import bunyod.profunctors.utils.config.Configuration.PasswordSalt
import cats.effect.Sync
import cats.implicits._
import javax.crypto.{Cipher, SecretKeyFactory}
import javax.crypto.spec.{PBEKeySpec, SecretKeySpec}

object CryptoService {

  def make[F[_]: Sync](secret: PasswordSalt): F[CryptoAlgebra] =
    Sync[F]
      .delay {
        val salt = secret.value.value.value.getBytes("UTF-8")
        val keySpec = new PBEKeySpec("password".toCharArray, salt, 65536, 256)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
        val bytes = factory.generateSecret(keySpec).getEncoded
        val sKeySpec = new SecretKeySpec(bytes, "AES")
        val eCipher = EncryptCipher(Cipher.getInstance("AES"))
        eCipher.value.init(Cipher.ENCRYPT_MODE, sKeySpec)
        val dCipher = DecryptCipher(Cipher.getInstance("AES"))
        dCipher.value.init(Cipher.DECRYPT_MODE, sKeySpec)
        (eCipher, dCipher)
      }
      .map {
        case (ec, dc) =>
          new CryptoService(ec, dc)
      }

}

final class CryptoService private (
  eCipher: EncryptCipher,
  dCipher: DecryptCipher
) extends CryptoAlgebra {

  private val Key = "=DownInAHole="

  def encrypt(password: Password): EncryptedPassword = {
    val bytes = password.value.getBytes("UTF-8")
    val result = new String(eCipher.value.doFinal(bytes), "UTF-8")
    val removeNull = result.replaceAll("\\u0000", Key)
    EncryptedPassword(removeNull)
  }

  def decrypt(password: EncryptedPassword): Password = {
    val bytes = password.value.getBytes("UTF-8")
    val result = new String(dCipher.value.doFinal(bytes), "UTF-8")
    val insertNull = result.replaceAll(Key, "\\0000")
    Password(insertNull)
  }

}
