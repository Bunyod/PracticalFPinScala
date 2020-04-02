package bunyod.fp.domain.cart

import bunyod.fp.domain.auth.AuthPayloads.UserId
import bunyod.fp.domain.cart.CartPayloads._
import bunyod.fp.domain.items.ItemsPayloads.ItemId

class ShoppingCartService[F[_]](shoppingCartAlgebra: ShoppingCartAlgebra[F]) {

  def add(userId: UserId, itemId: ItemId, quantity: Quantity): F[Unit] =
    shoppingCartAlgebra.add(userId, itemId, quantity)

  def delete(userId: UserId): F[Unit] =
    shoppingCartAlgebra.delete(userId)

  def get(userId: UserId): F[CartTotal] =
    shoppingCartAlgebra.get(userId)

  def removeItem(userId: UserId, itemId: ItemId): F[Unit] =
    shoppingCartAlgebra.removeItem(userId, itemId)

  def update(userId: UserId, cart: Cart): F[Unit] =
    shoppingCartAlgebra.update(userId, cart)

}
