package bunyod.profunctors.domain.orders

import bunyod.profunctors.domain.cart.CartPayloads.Quantity
import bunyod.profunctors.domain.items.ItemsPayloads.ItemId
import io.estatico.newtype.macros.newtype
import java.util.UUID
import scala.util.control.NoStackTrace
import squants.market.Money

object OrdersPayloads {

  @newtype case class OrderId(uuid: UUID)
  @newtype case class PaymentId(uuid: UUID)

  case class Order(
    id: OrderId,
    pid: PaymentId,
    items: Map[ItemId, Quantity],
    toatl: Money
  )

  case object EmptyCartError extends NoStackTrace
  case class OrderError(cause: String) extends NoStackTrace
  case class PaymentError(cause: String) extends NoStackTrace

}
