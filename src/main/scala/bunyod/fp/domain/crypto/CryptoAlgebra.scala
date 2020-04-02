package bunyod.fp.domain.crypto

import bunyod.fp.domain.auth.AuthPayloads.{EncryptedPassword, Password}

trait CryptoAlgebra {

  def encrypt(value: Password): EncryptedPassword
  def decrypt(value: EncryptedPassword): Password

}
