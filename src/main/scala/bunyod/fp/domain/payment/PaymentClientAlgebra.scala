package bunyod.fp.domain.payment

import bunyod.fp.domain.orders.OrdersPayloads.PaymentId
import bunyod.fp.domain.payment.PaymentPayloads.Payment

trait PaymentClientAlgebra[F[_]] {

  def process(payment: Payment): F[PaymentId]

}
