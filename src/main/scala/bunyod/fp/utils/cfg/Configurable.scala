package bunyod.fp.utils.cfg

import bunyod.fp.utils.cfg.Configuration._
import cats.implicits._
import ciris._
import ciris.refined._
// import com.typesafe.config.ConfigFactory
import eu.timepit.refined.auto._
import eu.timepit.refined.cats._
import eu.timepit.refined.types.net.UserPortNumber
import eu.timepit.refined.types.string.NonEmptyString
// import pureconfig.ConfigSource
import scala.concurrent.duration.DurationInt

trait Configurable {
  val config: ConfigValue[Config] = Configurable.config
}

object Configurable {

  val config: ConfigValue[Config] =
    env("APP_ENV").as[AppEnvironment].option.flatMap {
//      case Some(AppEnvironment.Dev) | None =>
//        ConfigValue.default[Config](ConfigSource.default.loadOrThrow[Config])
      case _ =>
        (
          env("JWT_SECRET_KEY").as[NonEmptyString].secret,
          env("JWT_CLAIM").as[NonEmptyString].secret,
          env("ACCESS_TOKEN_SECRET_KEY").as[NonEmptyString].secret,
          env("ADMIN_USER_TOKEN").as[NonEmptyString].secret,
          env("PASSWORD_SALT").as[NonEmptyString].secret,
          env("REDIS_URI").as[NonEmptyString],
          env("PAYMENT_URI").as[NonEmptyString],
          env("POSTGRESQL_HOST").as[NonEmptyString],
          env("POSTGRESQL_PORT").as[UserPortNumber],
          env("POSTGRESQL_USER").as[NonEmptyString],
          env("POSTGRESQL_DATABASE").as[NonEmptyString]
        ).parMapN {
          (secretKey, claimStr, tokenKey, adminToken, salt, redisUri, paymentUri, pgHost, pgPort, pgUser, database) =>
            Config(
              AdminJwtCfg(
                JwtSecretKeyCfg(secretKey),
                JwtClaimCfg(claimStr),
                AdminUserTokenCfg(adminToken)
              ),
              JwtSecretKeyCfg(tokenKey),
              PasswordSaltCfg(salt),
              TokenExpirationCfg(30.minutes),
              ShoppingCartCfg(30.minutes),
              CheckoutCfg(
                retriesLimit = 3,
                retriesBackoff = 10.milliseconds
              ),
              PaymentCfg(PaymentURI(paymentUri)),
              HttpClientCfg(
                connectTimeout = 2.seconds,
                requestTimeout = 2.seconds
              ),
              HttpServerCfg(
                host = "0.0.0.0",
                port = 8080
              ),
              PostgreSQLCfg(
                host = pgHost,
                port = pgPort,
                user = pgUser,
                database = database,
                max = 10
              ),
              RedisCfg(RedisURI(redisUri))
            )
        }
    }

}
