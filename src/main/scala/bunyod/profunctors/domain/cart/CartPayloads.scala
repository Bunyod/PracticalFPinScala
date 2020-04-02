package bunyod.profunctors.domain.cart

import bunyod.profunctors.domain.auth.AuthPayloads.UserId
import bunyod.profunctors.domain.items.ItemsPayloads.{Item, ItemId}
import io.estatico.newtype.macros.newtype
import java.util.UUID
import scala.util.control.NoStackTrace
import squants.market.Money

object CartPayloads {

  @newtype case class Quantity(value: Int)
  @newtype case class Cart(items: Map[ItemId, Quantity])
  @newtype case class CartId(value: UUID)

  case class CartItem(item: Item, quantity: Quantity)
  case class CartTotal(items: List[CartItem], total: Money)

  case class CartNotFound(userId: UserId) extends NoStackTrace

}
