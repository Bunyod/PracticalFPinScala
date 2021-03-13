package bunyod.fp.domain.checkout

import bunyod.fp.domain.auth.AuthPayloads.UserId
import bunyod.fp.domain.cart.CartPayloads.{Cart, CartItem, CartTotal, Quantity}
import bunyod.fp.domain.cart.{ShoppingCartAlgebra, ShoppingCartService}
import bunyod.fp.domain.items.ItemsPayloads.ItemId
import bunyod.fp.domain.orders.OrdersPayloads.{EmptyCartError, Order, OrderError, OrderId, PaymentError, PaymentId}
import bunyod.fp.domain.orders.{OrdersAlgebra, OrdersService}
import bunyod.fp.domain.payment.PaymentPayloads.Payment
import bunyod.fp.domain.payment._
import bunyod.fp.effekts.Background
import bunyod.fp.suite.BackgroundTest

import scala.util.control.NoStackTrace
import bunyod.fp.suite.Generators._
import io.chrisdavenport.log4cats.Logger
//import bunyod.fp.suite._

import cats.effect._
import cats.effect.concurrent.Ref
import cats.implicits._
import retry.RetryPolicies._
import retry.RetryPolicy
import squants.market._
import weaver._
import weaver.scalacheck.Checkers

class CheckoutServiceSpec extends SimpleIOSuite with Checkers {

  val MaxRetries = 3
  val retryPolicy: RetryPolicy[IO] = limitRetries[IO](MaxRetries)

  def successfulClient(paymentId: PaymentId): PaymentClientAlgebra[IO] = new PaymentClientAlgebra[IO] {
    override def process(payment: Payment): IO[PaymentId] = IO.pure(paymentId)
  }

  val unreachableClient: PaymentClientAlgebra[IO] = new PaymentClientAlgebra[IO] {
    override def process(payment: Payment): IO[PaymentId] = IO.raiseError(PaymentError(""))
  }

  def recoveringClient(attemptsSoFar: Ref[IO, Int], paymentId: PaymentId): PaymentClientAlgebra[IO] =
    new PaymentClientAlgebra[IO] {
      override def process(payment: Payment): IO[PaymentId] =
        attemptsSoFar.get.flatMap {
          case n if n == 1 => IO.pure(paymentId)
          case _ => attemptsSoFar.update(_ + 1) *> IO.raiseError(PaymentError(""))
        }
    }

  val failingOrders: OrdersService[IO] = new OrdersService[IO](
    new TestOrdersRepository {
      override def create(userId: UserId, paymentId: PaymentId, items: List[CartItem], total: Money): IO[OrderId] =
        IO.raiseError(OrderError(""))
    }
  )

  val emptyCart: ShoppingCartService[IO] =
    new ShoppingCartService[IO](new TestCartRepository {
      override def get(userId: UserId): IO[CartTotal] =
        IO.pure(CartTotal(List.empty, USD(0)))
    })

  def failingCart(cartTotal: CartTotal): ShoppingCartService[IO] = new ShoppingCartService[IO](
    new TestCartRepository {
      override def get(userId: UserId): IO[CartTotal] = IO.pure(cartTotal)
      override def delete(userId: UserId): IO[Unit] = IO.raiseError(new NoStackTrace {})
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

  val gen = for {
    uid <- userIdGen
    pid <- paymentIdGen
    oid <- orderIdGen
    crt <- cartTotalGen
    crd <- cardGen
  } yield (uid, pid, oid, crt, crd)

  test("empty cart") {
    forall(gen) { case (uid, pid, oid, _, card) =>
      implicit val bg = BackgroundTest.NoOp
      import bunyod.fp.suite.LoggerSuite.NoOp

      new CheckoutService[IO](successfulClient(pid), emptyCart, successfulOrders(oid), retryPolicy)
        .checkout(uid, card)
        .attempt
        .map {
          case Left(EmptyCartError) => success
          case _ => failure("Cart was not empty as expected")
        }
    }
  }

  test("unreachable payment client") {
    import bunyod.fp.suite.LoggerSuite

    forall(gen) { case (uid, _, oid, ct, card) =>
      Ref.of[IO, List[String]](List.empty).flatMap { logs =>
        implicit val bg: Background[IO] = BackgroundTest.NoOp
        implicit val logger: Logger[IO] = LoggerSuite.acc(logs)
        new CheckoutService[IO](unreachableClient, successfulCart(ct), successfulOrders(oid), retryPolicy)
          .checkout(uid, card)
          .attempt
          .flatMap {
            case Left(PaymentError(_)) =>
              logs.get.map {
                case x :: xs => expect.all(x.contains("Giving up"), xs.size === MaxRetries)
                case _ => failure(s"Expected $MaxRetries")
              }
            case _ => IO.pure(failure("Expected payment error"))
          }
      }
    }
  }

  test("failing payment client succeeds after one retry") {
    import bunyod.fp.suite.LoggerSuite
    forall(gen) { case (uid, pid, oid, ct, card) =>
      Ref.of[IO, List[String]](List.empty).flatMap { logs =>
        Ref.of[IO, Int](0).flatMap { ref =>
          implicit val bg = BackgroundTest.NoOp
          implicit val logger: Logger[IO] = LoggerSuite.acc(logs)
          new CheckoutService[IO](recoveringClient(ref, pid), successfulCart(ct), successfulOrders(oid), retryPolicy)
            .checkout(uid, card)
            .attempt
            .flatMap {
              case Right(id) =>
                logs.get.map { xs =>
                  expect.all(id === oid, xs.size === 1)
                }
              case Left(_) => IO.pure(failure("Expected PayemtnId"))
            }
        }
      }
    }
  }

  test("cannot create order, run in the background") {
    import bunyod.fp.suite.LoggerSuite
    forall(gen) { case (uid, pid, _, ct, card) =>
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
                    expect.all(
                      x.contains("Rescheduling"),
                      y.contains("Giving up"),
                      xs.size === MaxRetries,
                      c === 1
                    )
                  case _ => failure(s"Expected $MaxRetries retries and reschedule")
                }
              case _ => IO.pure(fail("Expected order error"))
            }
        }
      }
    }
  }

  test("failing to delete cart does not affect checkout") {
    implicit val bg = BackgroundTest.NoOp
    import bunyod.fp.suite.LoggerSuite.NoOp

    forall(gen) { case (uid, pid, oid, ct, card) =>
      new CheckoutService[IO](successfulClient(pid), failingCart(ct), successfulOrders(oid), retryPolicy)
        .checkout(uid, card)
        .map(expect.same(oid, _))
    }
  }

  test("successful checkout") {
    implicit val bg = BackgroundTest.NoOp
    import bunyod.fp.suite.LoggerSuite.NoOp
    forall(gen) { case (uid, pid, oid, ct, card) =>
      new CheckoutService[IO](successfulClient(pid), successfulCart(ct), successfulOrders(oid), retryPolicy)
        .checkout(uid, card)
        .map(expect.same(oid, _))
    }
  }

}

protected class TestOrdersRepository extends OrdersAlgebra[IO] {

  override def get(userId: UserId, orderId: OrderId): IO[Option[Order]] = ???
  override def findBy(userId: UserId): IO[List[Order]] = ???
  override def create(userId: UserId, paymentId: PaymentId, items: List[CartItem], total: Money): IO[OrderId] = ???

}

protected class TestCartRepository extends ShoppingCartAlgebra[IO] {
  override def add(userId: UserId, itemId: ItemId, quantity: Quantity): IO[Unit] = ???
  override def get(userId: UserId): IO[CartTotal] = ???
  override def delete(userId: UserId): IO[Unit] = ???
  override def removeItem(userId: UserId, itemId: ItemId): IO[Unit] = ???
  override def update(userId: UserId, cart: Cart): IO[Unit] = ???
}
