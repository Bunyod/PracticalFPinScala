package bunyod.fp.infrastructure.clients

import bunyod.fp.domain.orders.OrdersPayloads.PaymentId
import bunyod.fp.domain.payment.PaymentPayloads.Payment

trait PaymentClient[F[_]] {

  def process(payment: Payment): F[PaymentId]

}
