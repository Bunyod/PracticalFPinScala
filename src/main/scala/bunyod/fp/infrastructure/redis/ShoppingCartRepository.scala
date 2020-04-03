package bunyod.fp.infrastructure.redis

import bunyod.fp.domain.auth.AuthPayloads
import bunyod.fp.domain.auth.AuthPayloads.UserId
import bunyod.fp.domain.cart.CartPayloads._
import bunyod.fp.domain.cart._
import bunyod.fp.domain.items.ItemsPayloads.ItemId
import bunyod.fp.domain.items._
import bunyod.fp.effects._
import bunyod.fp.utils.cfg.Configuration.ShoppingCartCfg
import cats.implicits._
import dev.profunktor.redis4cats.algebra.RedisCommands
import squants.market._

class ShoppingCartRepository[F[_]: GenUUID: MonadThrow](
  items: ItemsAlgebra[F],
  redis: RedisCommands[F, String, String],
  expCfg: ShoppingCartCfg
) extends ShoppingCartAlgebra[F] {

  override def add(
    userId: UserId,
    itemId: ItemId,
    quantity: Quantity
  ): F[Unit] =
    redis.hSet(userId.value.toString, itemId.value.toString, quantity.value.toString) *>
      redis.expire(userId.value.toString, expCfg.expiration)

  override def delete(userId: AuthPayloads.UserId): F[Unit] = redis.del(userId.value.toString)

  override def get(userId: UserId): F[CartPayloads.CartTotal] =
    redis.hGetAll(userId.value.toString).flatMap { it =>
      it.toList
        .traverseFilter {
          case (k, v) =>
            for {
              id <- GenUUID[F].read[ItemId](k)
              qt <- ApThrow[F].catchNonFatal(Quantity(v.toInt))
              rs <- items.findById(id).map(_.map(i => CartItem(i, qt)))
            } yield rs
        }
        .map(items => CartTotal(items, calcTotal(items)))

    }

  override def removeItem(userId: UserId, itemId: ItemId): F[Unit] =
    redis.hDel(userId.value.toString, itemId.value.toString)

  override def update(userId: UserId, cart: Cart): F[Unit] = redis.hGetAll(userId.value.toString).flatMap { items =>
    items.toList
      .traverse_ {
        case (k, _) =>
          GenUUID[F].read[ItemId](k).flatMap { id =>
            cart.items.get(id).traverse_(q => redis.hSet(userId.value.toString, k, q.value.toString))
          }

      } *>
      redis.expire(userId.value.toString, expCfg.expiration)
  }

  private def calcTotal(items: List[CartItem]): Money =
    USD(
      items.foldMap(i => i.item.price.value * i.quantity.value)
    )

}
