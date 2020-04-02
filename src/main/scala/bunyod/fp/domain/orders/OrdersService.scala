package bunyod.fp.domain.orders

import bunyod.fp.domain.auth.AuthPayloads.UserId
import bunyod.fp.domain.cart.CartPayloads.CartItem
import bunyod.fp.domain.orders.OrdersPayloads.{Order, OrderId, PaymentId}
import squants.market.Money

class OrdersService[F[_]](ordersRepo: OrdersAlgebra[F]) {

  def get(userId: UserId, orderId: OrderId): F[Option[Order]] =
    ordersRepo.get(userId, orderId)

  def findBy(userId: UserId): F[List[Order]] =
    ordersRepo.findBy(userId)

  def create(
    userId: UserId,
    paymentId: PaymentId,
    items: List[CartItem],
    total: Money
  ): F[OrderId] =
    ordersRepo.create(userId, paymentId, items, total)

}
