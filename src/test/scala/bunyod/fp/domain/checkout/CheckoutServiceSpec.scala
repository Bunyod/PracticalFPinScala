package bunyod.fp.domain.checkout

import bunyod.fp.domain.auth.AuthPayloads._
import bunyod.fp.domain.cart.CartPayloads._
import bunyod.fp.domain.cart._
import bunyod.fp.domain.checkout.CheckoutPayloads._
import bunyod.fp.domain.items.ItemsPayloads._
import bunyod.fp.domain.orders.OrdersPayloads._
import bunyod.fp.domain.orders._
import bunyod.fp.domain.payment.PaymentPayloads._
import bunyod.fp.domain.payment._
import bunyod.fp.effects.Background
import bunyod.fp.logger.LoggerSuite
import bunyod.fp.suite.Arbitraries._
import bunyod.fp.suite.{BackgroundTest, PureTestSuite}
import cats.effect._
import cats.effect.concurrent.Ref
import cats.implicits.{catsSyntaxEq => _, _}
import io.chrisdavenport.log4cats.Logger
import retry.RetryPolicy
import retry.RetryPolicies._
import squants.market._

class CheckoutServiceSpec extends PureTestSuite {

  val MaxRetries = 3
  val retryPolicy: RetryPolicy[IO] = limitRetries[IO](MaxRetries)

  def successfulClient(paymentId: PaymentId): PaymentClientService[IO] =
    new PaymentClientService[IO](new PaymentClientAlgebra[IO] {
      override def process(payment: Payment): IO[PaymentId] =
        IO.pure(paymentId)
    })

  val unreachableClient: PaymentClientService[IO] = new PaymentClientService[IO](
    new PaymentClientAlgebra[IO] {
      override def process(payment: Payment): IO[PaymentId] =
        IO.raiseError(PaymentError("Unreachable"))
    }
  )

  def recoveringClient(attemptsSoFar: Ref[IO, Int], paymentId: PaymentId): PaymentClientService[IO] =
    new PaymentClientService[IO](new PaymentClientAlgebra[IO] {
      override def process(payment: Payment): IO[PaymentId] =
        attemptsSoFar.get.flatMap {
          case n if n == 1 => IO.pure(paymentId)
          case _ => attemptsSoFar.update(_ + 1) *> IO.raiseError(PaymentError("Failed"))
        }
    })

  def failingOrders: OrdersService[IO] = new OrdersService[IO](
    new TestOrdersRepository {
      override def create(userId: UserId, paymentId: PaymentId, items: List[CartItem], total: Money): IO[OrderId] =
        IO.raiseError(OrderError(""))
    }
  )

  def emptyCart: ShoppingCartService[IO] =
    new ShoppingCartService[IO](new TestCartRepository {
      override def get(userId: UserId): IO[CartTotal] =
        IO.pure(CartTotal(List.empty, USD(0)))
    })

  def failingCart(cartTotal: CartTotal): ShoppingCartService[IO] = new ShoppingCartService[IO](
    new TestCartRepository {
      override def get(userId: UserId): IO[CartTotal] = IO.pure(cartTotal)
      override def delete(userId: UserId): IO[Unit] = IO.raiseError(new Exception("Failed"))
    }
  )

  def successfulCart(cartTotal: CartTotal): ShoppingCartService[IO] = new ShoppingCartService[IO](
    new TestCartRepository {
      override def get(userId: UserId): IO[CartTotal] = IO.pure(cartTotal)
      override def delete(userId: UserId): IO[Unit] = IO.unit

    }
  )

  def successfulOrders(orderId: OrderId): OrdersService[IO] = new OrdersService[IO](
    new TestOrdersRepository {
      override def create(userId: UserId, paymentId: PaymentId, items: List[CartItem], total: Money): IO[OrderId] =
        IO.pure(orderId)
    }
  )

  forAll { (uid: UserId, pid: PaymentId, oid: OrderId, card: Card) =>
    spec("empty cart") {
      implicit val bg = BackgroundTest.NoOp
      import bunyod.fp.logger.LoggerSuite.NoOp
      new CheckoutService[IO](successfulClient(pid), emptyCart, successfulOrders(oid), retryPolicy)
        .checkout(uid, card)
        .attempt
        .map {
          case Left(EmptyCartError) => assert(true)
          case _ => fail("Cart was not empty as expected")
        }
    }
  }

  forAll { (uid: UserId, oid: OrderId, ct: CartTotal, card: Card) =>
    spec("unreachable payment client") {
      Ref.of[IO, List[String]](List.empty).flatMap { logs =>
        implicit val bg: Background[IO] = BackgroundTest.NoOp
        implicit val logger: Logger[IO] = LoggerSuite.acc(logs)
        new CheckoutService[IO](unreachableClient, successfulCart(ct), successfulOrders(oid), retryPolicy)
          .checkout(uid, card)
          .attempt
          .flatMap {
            case Left(PaymentError(_)) =>
              logs.get.map {
                case x :: xs => assert(x.contains("Giving up") && xs.size === MaxRetries)
                case _ => fail(s"Expected $MaxRetries")
              }
            case _ => fail("Expected payment error")
          }
      }
    }
  }

  forAll { (uid: UserId, pid: PaymentId, oid: OrderId, ct: CartTotal, card: Card) =>
    spec("failing payment client succeeds after one retry") {
      Ref.of[IO, List[String]](List.empty).flatMap { logs =>
        Ref.of[IO, Int](0).flatMap { ref =>
//          implicit val bg: Background[IO] = BackgroundTest.NoOp
          implicit val logger: Logger[IO] = LoggerSuite.acc(logs)
          new CheckoutService[IO](recoveringClient(ref, pid), successfulCart(ct), successfulOrders(oid), retryPolicy)
            .checkout(uid, card)
            .attempt
            .flatMap {
              case Right(id) =>
                logs.get.map(xs => assert(id.uuid === oid.uuid && xs.size === 1))
              case Left(_) => fail("Expected PayemtnId")
            }
        }
      }
    }
  }

  forAll { (uid: UserId, pid: PaymentId, ct: CartTotal, card: Card) =>
    spec("cannot create order, run in the background") {
      Ref.of[IO, Int](0).flatMap { ref =>
        Ref.of[IO, List[String]](List.empty).flatMap { logs =>
          implicit val bg = BackgroundTest.counter(ref)
          implicit val logger = LoggerSuite.acc(logs)
          new CheckoutService[IO](successfulClient(pid), successfulCart(ct), failingOrders, retryPolicy)
            .checkout(uid, card)
            .attempt
            .flatMap {
              case Left(OrderError(_)) =>
                (ref.get, logs.get).mapN {
                  case (c, (x :: y :: xs)) =>
                    assert(
                      x.contains("Rescheduling") &&
                        y.contains("Giving up") &&
                        xs.size === MaxRetries &&
                        c === 1
                    )
                  case _ => fail(s"Expected $MaxRetries retries and reschedule")
                }
              case _ => fail("Expected order error")
            }
        }
      }
    }
  }

  forAll { (uid: UserId, pid: PaymentId, oid: OrderId, ct: CartTotal, card: Card) =>
    spec("failing to delete cart does not affect checkout") {
      import LoggerSuite._
      new CheckoutService[IO](successfulClient(pid), failingCart(ct), successfulOrders(oid), retryPolicy)
        .checkout(uid, card)
        .map(id => assert(id.uuid === oid.uuid))
    }
  }

  forAll { (uid: UserId, pid: PaymentId, oid: OrderId, ct: CartTotal, card: Card) =>
    spec("successful checkout") {
      import LoggerSuite._
      new CheckoutService[IO](successfulClient(pid), successfulCart(ct), successfulOrders(oid), retryPolicy)
        .checkout(uid, card)
        .map(id => assert(id.uuid === oid.uuid))
    }
  }

}

protected class TestOrdersRepository extends OrdersAlgebra[IO] {

  override def get(userId: UserId, orderId: OrderId): IO[Option[Order]] = ???

  override def findBy(userId: UserId): IO[List[Order]] = ???

  override def create(
    userId: UserId,
    paymentId: PaymentId,
    items: List[CartItem],
    total: Money
  ): IO[OrderId] = ???

}

protected class TestCartRepository extends ShoppingCartAlgebra[IO] {

  override def add(userId: UserId, itemId: ItemId, quantity: Quantity): IO[Unit] = ???
  override def delete(userId: UserId): IO[Unit] = ???
  override def get(userId: UserId): IO[CartTotal] = ???
  override def removeItem(userId: UserId, itemId: ItemId): IO[Unit] = ???
  override def update(userId: UserId, cart: Cart): IO[Unit] = ???
}
