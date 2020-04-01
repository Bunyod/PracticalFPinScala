package bunyod.profunctors.domain.payment

import bunyod.profunctors.domain.auth.AuthPayloads.UserId
import bunyod.profunctors.domain.checkout.CheckoutPayloads.Card
import squants.market.Money

object PaymentPayloads {

  case class Payment(
    id: UserId,
    total: Money,
    card: Card
  )

}
