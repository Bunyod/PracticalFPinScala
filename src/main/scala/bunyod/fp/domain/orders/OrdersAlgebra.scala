package bunyod.fp.domain.orders

import bunyod.fp.domain.auth.AuthPayloads.UserId
import bunyod.fp.domain.cart.CartPayloads.CartItem
import bunyod.fp.domain.orders.OrdersPayloads.{Order, OrderId, PaymentId}
import squants.market.Money

trait OrdersAlgebra[F[_]] {

  def get(userId: UserId, orderId: OrderId): F[Option[Order]]

  def findBy(userId: UserId): F[List[Order]]

  def create(
    userId: UserId,
    paymentId: PaymentId,
    items: List[CartItem],
    total: Money
  ): F[OrderId]

}
