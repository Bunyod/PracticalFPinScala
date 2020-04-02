package bunyod.fp.domain.payment

import bunyod.fp.domain.auth.AuthPayloads.UserId
import bunyod.fp.domain.checkout.CheckoutPayloads.Card
import squants.market.Money

object PaymentPayloads {

  case class Payment(
    id: UserId,
    total: Money,
    card: Card
  )

}
