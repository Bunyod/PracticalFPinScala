package bunyod.profunctors.domain.cart

import bunyod.profunctors.domain.auth.AuthPayloads.UserId
import bunyod.profunctors.domain.cart.CartPayloads.{Cart, CartTotal, Quantity}
import bunyod.profunctors.domain.items.ItemsPayloads.ItemId

trait ShoppingCartAlgebra[F[_]] {

  def add(userId: UserId, itemId: ItemId, quantity: Quantity): F[Unit]
  def delete(userId: UserId): F[Unit]
  def get(userId: UserId): F[CartTotal]
  def removeItem(userId: UserId, itemId: ItemId): F[Unit]
  def update(userId: UserId, cart: Cart): F[Unit]

}
