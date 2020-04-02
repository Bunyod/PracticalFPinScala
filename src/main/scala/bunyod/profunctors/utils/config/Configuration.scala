package bunyod.profunctors.utils.config

import ciris.Secret
import eu.timepit.refined.types.string.NonEmptyString
import io.estatico.newtype.macros.newtype

object Configuration {

  @newtype case class JwtSecretKeyConfig(
    value: Secret[NonEmptyString]
  )

  @newtype case class PasswordSalt(value: Secret[NonEmptyString])

}
