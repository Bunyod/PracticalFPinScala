package bunyod.fp.domain
package orders

import java.util.UUID
import scala.util.control.NoStackTrace

import bunyod.fp.domain.cart.CartPayloads._
import bunyod.fp.domain.items.ItemsPayloads._
import bunyod.fp.effekts.uuid
import derevo.cats._
import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import io.circe.Decoder
import io.estatico.newtype.macros.newtype
import squants.market.Money

object OrdersPayloads {

  @derive(decoder, encoder, eqv, show, uuid)
  @newtype case class OrderId(value: UUID)

  @derive(encoder, eqv, show, uuid)
  @newtype case class PaymentId(value: UUID)
  object PaymentId {
    implicit val jsonDecoder: Decoder[PaymentId] =
      Decoder.forProduct1("paymentId")(PaymentId.apply)
  }

  @derive(decoder, encoder)
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
