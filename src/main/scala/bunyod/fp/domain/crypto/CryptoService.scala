package bunyod.fp.domain.crypto

import bunyod.fp.domain.auth.AuthPayloads._
import bunyod.fp.utils.cfg.Configuration.PasswordSaltCfg
import cats.effect.Sync
import cats.implicits._

import java.security.SecureRandom
import java.util.Base64
import javax.crypto.{Cipher, SecretKeyFactory}
import javax.crypto.spec.{IvParameterSpec, PBEKeySpec, SecretKeySpec}

object CryptoService {

  def make[F[_]: Sync](secret: PasswordSaltCfg): F[CryptoAlgebra] =
    Sync[F]
      .delay {
        val salt = secret.value.value.getBytes("UTF-8")
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
      .map { case (ec, dc) =>
        new CryptoService(ec, dc)
      }

}

object LiveCrypto {
  def make[F[_]: Sync](secret: PasswordSaltCfg): F[CryptoAlgebra] =
    Sync[F]
      .delay {
        val random = new SecureRandom()
        val ivBytes = new Array[Byte](16)
        random.nextBytes(ivBytes)
        val iv = new IvParameterSpec(ivBytes)
        val salt = secret.value.value.getBytes("UTF-8")
        val keySpec = new PBEKeySpec("passowrd".toCharArray, salt, 65536, 256)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
        val bytes = factory.generateSecret(keySpec).getEncoded
        val sKeySpec = new SecretKeySpec(bytes, "AES")
        val eCipher = EncryptCipher(Cipher.getInstance("AES/CBC/PKCS5Padding"))
        eCipher.value.init(Cipher.ENCRYPT_MODE, sKeySpec, iv)
        val dCipher = DecryptCipher(Cipher.getInstance("AES/CBC/PKCS5Padding"))
        dCipher.value.init(Cipher.DECRYPT_MODE, sKeySpec, iv)
        (eCipher, dCipher)
      }
      .map { case (ec, dc) =>
        new LiveCryptoRepository(ec, dc)
      }
}

final class LiveCryptoRepository(
  eCipher: EncryptCipher,
  dCipher: DecryptCipher
) extends CryptoAlgebra {

  def encrypt(password: Password): EncryptedPassword = {
    val base64 = Base64.getEncoder()
    val bytes = password.value.getBytes("UTF-8")
    val result = new String(base64.encode(eCipher.value.doFinal(bytes)), "UTF-8")
    EncryptedPassword(result)
  }

  def decrypt(password: EncryptedPassword): Password = {
    val base64 = Base64.getDecoder()
    val bytes = base64.decode(password.value.getBytes("UTF-8"))
    val result = new String(dCipher.value.doFinal(bytes), "UTF-8")
    Password(result)
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
