package bunyod.profunctors.infrastructure.clients

import bunyod.profunctors.domain.orders.OrdersPayloads.PaymentId
import bunyod.profunctors.domain.payment.PaymentPayloads.Payment

trait PaymentClient[F[_]] {

  def process(payment: Payment): F[PaymentId]

}
