package bunyod.fp.domain
package payment

import bunyod.fp.domain.auth.AuthPayloads.UserId
import bunyod.fp.domain.checkout.CheckoutPayloads.Card
import squants.market.Money
import derevo.circe.magnolia.encoder
import derevo.derive

object PaymentPayloads {

  @derive(encoder)
  case class Payment(
    id: UserId,
    total: Money,
    card: Card
  )

}
