package bunyod.profunctors.domain.payment

import bunyod.profunctors.domain.orders.OrdersPayloads.PaymentId
import bunyod.profunctors.domain.payment.PaymentPayloads.Payment

trait PaymentClientAlgebra[F[_]] {

  def process(payment: Payment): F[PaymentId]

}
