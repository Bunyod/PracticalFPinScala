package bunyod.fp.domain.orders

import bunyod.fp.domain.cart.CartPayloads.Quantity
import bunyod.fp.domain.items.ItemsPayloads.ItemId
import io.estatico.newtype.macros.newtype
import java.util.UUID
import scala.util.control.NoStackTrace
import squants.market.Money

object OrdersPayloads {

  @newtype case class OrderId(value: UUID)
  @newtype case class PaymentId(value: UUID)

  case class Order(
    id: OrderId,
    paymentId: PaymentId,
    items: Map[ItemId, Quantity],
    total: Money
  )

  case object EmptyCartError extends NoStackTrace
  case class OrderError(cause: String) extends NoStackTrace
  case class PaymentError(cause: String) extends NoStackTrace

}
