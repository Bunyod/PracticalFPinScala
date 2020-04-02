package bunyod.profunctors.domain.payment

import bunyod.profunctors.domain.orders.OrdersPayloads.PaymentId
import bunyod.profunctors.domain.payment.PaymentPayloads.Payment

class PaymentClientService[F[_]](paymentsRepo: PaymentClientAlgebra[F]) {

  def process(payment: Payment): F[PaymentId] =
    paymentsRepo.process(payment)

}
