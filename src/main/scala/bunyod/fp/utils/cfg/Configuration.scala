package bunyod.fp.utils.cfg

import ciris.Secret
import enumeratum._
import enumeratum.EnumEntry._
import eu.timepit.refined.types.net.UserPortNumber
import eu.timepit.refined.types.numeric.PosInt
import eu.timepit.refined.types.string.NonEmptyString
import io.estatico.newtype.macros.newtype
import scala.concurrent.duration.FiniteDuration

object Configuration {

  sealed abstract class AppEnvironment extends EnumEntry with Lowercase

  object AppEnvironment extends Enum[AppEnvironment] with CirisEnum[AppEnvironment] {
    case object Dev extends AppEnvironment
    case object Stage extends AppEnvironment
    case object Prod extends AppEnvironment

    val values = findValues

  }

  case class Config(
    adminJwt: AdminJwtCfg,
    jwtSecretKey: JwtSecretKeyCfg,
    passwordSalt: PasswordSaltCfg,
    tokenExpiration: TokenExpirationCfg,
    shoppingCart: ShoppingCartCfg,
    checkout: CheckoutCfg,
    payment: PaymentCfg,
    httpClient: HttpClientCfg,
    httpServer: HttpServerCfg,
    postgres: PostgreSQLCfg,
    redis: RedisCfg
  )

  @newtype case class JwtSecretKeyCfg(
    value: Secret[NonEmptyString]
  )

  @newtype case class AdminUserTokenCfg(value: Secret[NonEmptyString])
  @newtype case class JwtClaimCfg(value: Secret[NonEmptyString])

  case class AdminJwtCfg(
    secretKey: JwtSecretKeyCfg,
    claim: JwtClaimCfg,
    adminToken: AdminUserTokenCfg
  )
  @newtype case class PasswordSaltCfg(value: Secret[NonEmptyString])
  @newtype case class TokenExpirationCfg(value: FiniteDuration)
  @newtype case class ShoppingCartCfg(expiration: FiniteDuration)

  case class CheckoutCfg(
    retriesLimit: PosInt,
    retriesBackoff: FiniteDuration
  )

  @newtype case class PaymentURI(value: NonEmptyString)
  @newtype case class PaymentCfg(uri: PaymentURI)

  case class HttpServerCfg(
    host: NonEmptyString,
    port: UserPortNumber
  )

  case class HttpClientCfg(
    connectTimeout: FiniteDuration,
    requestTimeout: FiniteDuration
  )

  case class PostgreSQLCfg(
    host: NonEmptyString,
    port: UserPortNumber,
    user: NonEmptyString,
    database: NonEmptyString,
    max: PosInt
  )

  @newtype case class RedisURI(value: NonEmptyString)
  @newtype case class RedisCfg(uri: RedisURI)

}
