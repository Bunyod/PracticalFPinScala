package bunyod.profunctors.domain.payment

import bunyod.profunctors.domain.auth.auth.UserId
import bunyod.profunctors.domain.checkout.checkout.Card
import squants.market.Money

object payment {

  case class Payment(
    id: UserId,
    total: Money,
    card: Card
  )

}
