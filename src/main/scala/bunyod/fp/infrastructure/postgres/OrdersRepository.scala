package bunyod.fp.infrastructure.postgres

import bunyod.fp.domain.auth.AuthPayloads.UserId
import bunyod.fp.domain.cart.CartPayloads._
import bunyod.fp.domain.items.ItemsPayloads.ItemId
import bunyod.fp.domain.orders.OrdersPayloads._
import bunyod.fp.domain.orders._
import bunyod.fp.effekts.GenUUID
import bunyod.fp.http.utils.json._
import bunyod.fp.utils.extensions.Skunkx._

import cats.effect._
import cats.syntax.all._
import skunk._
import skunk.codec.all._
import skunk.circe.codec.all._
import skunk.implicits._
import squants.market._

class OrdersRepository[F[_]: Sync: BracketThrow: GenUUID](
  sessionPool: Resource[F, Session[F]]
) extends OrdersAlgebra[F] {

  import OrdersRepository._

  override def get(userId: UserId, orderId: OrderId): F[Option[Order]] =
    sessionPool.use(session => session.prepare(selectByUserIdAndOrderId).use(q => q.option(userId ~ orderId)))

  override def findByUserId(userId: UserId): F[List[Order]] =
    sessionPool.use(session => session.prepare(selectByUserId).use(q => q.stream(userId, 1024).compile.toList))

  override def create(
    userId: UserId,
    paymentId: PaymentId,
    items: List[CartItem],
    total: Money
  ): F[OrderId] =
    sessionPool.use { session =>
      session.prepare(insertOrder).use { cmd =>
        GenUUID[F].make[OrderId].flatMap { id =>
          val itMap = items.map(ci => ci.item.uuid -> ci.quantity).toMap
          val order = Order(id, paymentId, itMap, total)
          cmd.execute(userId ~ order).as(id)
        }
      }
    }

}

object LiveOrderRepository {
  def make[F[_]: Sync](
    sessionPool: Resource[F, Session[F]]
  ): F[OrdersAlgebra[F]] =
    Sync[F].delay(
      new OrdersRepository[F](sessionPool)
    )
}

object OrdersRepository {

  val decoder: Decoder[Order] =
    (
      uuid.cimap[OrderId] ~ uuid ~ uuid.cimap[PaymentId] ~
        jsonb[Map[ItemId, Quantity]] ~ numeric.map(USD.apply)
    ).map { case o ~ _ ~ p ~ i ~ t =>
      Order(o, p, i, t)
    }

  val encoder: Encoder[UserId ~ Order] =
    (
      uuid.cimap[OrderId] ~ uuid.cimap[UserId] ~ uuid.cimap[PaymentId] ~
        jsonb[Map[ItemId, Quantity]] ~ numeric.contramap[Money](_.amount)
    ).contramap { case id ~ o =>
      o.uuid ~ id ~ o.paymentId ~ o.items ~ o.total
    }

  val selectByUserId: Query[UserId, Order] =
    sql"""
        SELECT * FROM orders
        WHERE user_id = ${uuid.cimap[UserId]}
       """.query(decoder)

  val selectByUserIdAndOrderId: Query[UserId ~ OrderId, Order] =
    sql"""
        SELECT * FROM orders
        WHERE user_id = ${uuid.cimap[UserId]}
        AND uuid = ${uuid.cimap[OrderId]}
       """.query(decoder)

  val insertOrder: Command[UserId ~ Order] =
    sql"""
        INSERT INTO orders
        VALUES ($encoder)
       """.command

}
