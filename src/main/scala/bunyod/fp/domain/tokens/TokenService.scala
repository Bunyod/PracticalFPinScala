package bunyod.fp.domain.tokens

import bunyod.fp.effects.GenUUID
import bunyod.fp.utils.config.Configuration.JwtSecretKeyConfig
import cats.effect.Sync
import cats.implicits._
import io.circe.syntax._
import pdi.jwt._
import dev.profunktor.auth.jwt._
import pdi.jwt.JwtClaim
import scala.concurrent.duration.FiniteDuration

class TokenSyncService[F[_]: GenUUID: Sync](
  config: JwtSecretKeyConfig,
  exp: FiniteDuration
)(implicit val env: java.time.Clock)
  extends TokensAlgebra[F] {

  def create: F[JwtToken] =
    for {
      uuid <- GenUUID[F].make
      claim <- Sync[F].delay(JwtClaim(uuid.asJson.noSpaces).issuedNow.expiresIn(exp.toMillis))
      secretKey = JwtSecretKey(config.value.value.value)
      token <- jwtEncode[F](claim, secretKey, JwtAlgorithm.HS256)
    } yield token
}
