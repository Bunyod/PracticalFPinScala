package bunyod.profunctors.domain.crypto

import bunyod.profunctors.domain.auth.AuthPayloads.{EncryptedPassword, Password}

trait CryptoAlgebra {

  def encrypt(value: Password): EncryptedPassword
  def decrypt(value: EncryptedPassword): Password

}
