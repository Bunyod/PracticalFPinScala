package bunyod.profunctors.domain.cart

import bunyod.profunctors.domain.auth.auth.UserId
import bunyod.profunctors.domain.cart.cart.{Cart, CartTotal, Quantity}
import bunyod.profunctors.domain.items.items.ItemId

trait ShoppingCart[F[_]] {

  def add(userId: UserId, itemId: ItemId, quantity: Quantity): F[Unit]
  def delete(userId: UserId): F[Unit]
  def get(userId: UserId): F[CartTotal]
  def removeItem(userId: UserId, itemId: ItemId): F[Unit]
  def update(userId: UserId, cart: Cart): F[Unit]

}
