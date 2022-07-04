package bunyod.fp.infrastructure.clients

import bunyod.fp.domain.orders.OrdersPayloads._
import bunyod.fp.domain.payment.PaymentClientAlgebra
import bunyod.fp.domain.payment.PaymentPayloads.Payment
import bunyod.fp.utils.cfg.Configuration.PaymentCfg
import cats.effect.Async
import cats.implicits._
import org.http4s._
import org.http4s.circe._
import org.http4s.client._
import org.http4s.client.dsl.Http4sClientDsl
import bunyod.fp.http.utils.json._

class PaymentClientRepository[F[_]: Async: JsonDecoder](
  cfg: PaymentCfg,
  client: Client[F]
) extends PaymentClientAlgebra[F]
  with Http4sClientDsl[F] {

  def process(payment: Payment): F[PaymentId] =
    Uri.fromString(cfg.uri.value + "/payments").liftTo[F].flatMap { uri =>
      val request = Request[F](method = Method.POST, uri = uri)
        .withEntity(payment)
        client.run(request).use { r =>
          if (r.status == Status.Ok || r.status == Status.Conflict) {
            r.asJsonDecode[PaymentId]
          }
          else {
            PaymentError(
              Option(r.status.reason).getOrElse("Unknown")
            ).raiseError[F, PaymentId]
          }
      }
    }

}
