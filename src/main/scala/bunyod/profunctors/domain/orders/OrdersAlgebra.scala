package bunyod.profunctors.domain.orders

import bunyod.profunctors.domain.auth.AuthPayloads.UserId
import bunyod.profunctors.domain.cart.CartPayloads.CartItem
import bunyod.profunctors.domain.orders.OrdersPayloads.{Order, OrderId, PaymentId}
import squants.market.Money

trait OrdersAlgebra[F[_]] {

  def get(
    userId: UserId,
    orderId: OrderId
  ): F[Option[Order]]

  def findBy(userId: UserId): F[List[Order]]

  def create(
    userId: UserId,
    paymentId: PaymentId,
    items: List[CartItem],
    total: Money
  ): F[OrderId]

}
