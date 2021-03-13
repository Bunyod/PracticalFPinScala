package bunyod.fp.http

import bunyod.fp.domain.payment.PaymentClientAlgebra
import bunyod.fp.infrastructure.clients.PaymentClientRepository
import bunyod.fp.utils.cfg.Configuration.PaymentCfg
import cats.effect._
import org.http4s.circe.JsonDecoder
import org.http4s.client.Client

object HttpClients {
  def make[F[_]: BracketThrow: JsonDecoder](
    cfg: PaymentCfg,
    client: Client[F]
  ): HttpClients[F] =
    new HttpClients[F] {
      def payment: PaymentClientAlgebra[F] = PaymentClientRepository.make[F](cfg, client)
    }
}

trait HttpClients[F[_]] {
  def payment: PaymentClientAlgebra[F]
}
