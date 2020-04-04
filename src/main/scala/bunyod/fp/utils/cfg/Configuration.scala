package bunyod.fp.utils.cfg

import enumeratum._
import enumeratum.EnumEntry._
import eu.timepit.refined.types.net.UserPortNumber
import eu.timepit.refined.types.numeric.PosInt
import eu.timepit.refined.types.string.NonEmptyString
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
    userJwt: UserJwtCfg,
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

  case class AdminJwtCfg(
    secretKey: NonEmptyString,
    claim: NonEmptyString,
    adminToken: NonEmptyString
  )
  case class UserJwtCfg(
    secretKey: NonEmptyString,

  )
  case class PasswordSaltCfg(value: NonEmptyString)
  case class TokenExpirationCfg(value: FiniteDuration)
  case class ShoppingCartCfg(expiration: FiniteDuration)

  case class CheckoutCfg(
    retriesLimit: PosInt,
    retriesBackoff: FiniteDuration
  )

  case class PaymentCfg(uri: NonEmptyString)

  case class HttpServerCfg(
    host: NonEmptyString,
    port: UserPortNumber
  )

  case class HttpClientCfg(
    connectionTimeout: FiniteDuration,
    requestTimeout: FiniteDuration
  )

  case class PostgreSQLCfg(
    host: NonEmptyString,
    port: UserPortNumber,
    user: NonEmptyString,
    database: NonEmptyString,
    max: PosInt
  )

  case class RedisCfg(uri: NonEmptyString)

}
