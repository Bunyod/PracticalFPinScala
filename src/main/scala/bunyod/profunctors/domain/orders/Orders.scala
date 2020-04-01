package bunyod.profunctors.domain.orders

import bunyod.profunctors.domain.auth.auth.UserId
import bunyod.profunctors.domain.cart.cart.CartItem
import bunyod.profunctors.domain.orders.orders.{Order, OrderId, PaymentId}
import squants.market.Money

trait Orders[F[_]] {

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
