package bunyod.fp.domain.payment

import bunyod.fp.domain.orders.OrdersPayloads.PaymentId
import bunyod.fp.domain.payment.PaymentPayloads.Payment

class PaymentClientService[F[_]](paymentsRepo: PaymentClientAlgebra[F]) {

  def process(payment: Payment): F[PaymentId] = paymentsRepo.process(payment)

}
