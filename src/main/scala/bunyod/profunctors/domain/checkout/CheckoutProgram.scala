package bunyod.profunctors.domain.checkout

import bunyod.profunctors.domain.auth.auth.UserId
import bunyod.profunctors.domain.cart.{cart, ShoppingCart}
import bunyod.profunctors.domain.cart.cart.CartTotal
import bunyod.profunctors.domain.checkout.checkout.Card
import bunyod.profunctors.domain.orders.orders.{EmptyCartError, OrderError, OrderId, PaymentError, PaymentId}
import bunyod.profunctors.domain.orders.Orders
import bunyod.profunctors.domain.payment.PaymentClient
import bunyod.profunctors.domain.payment.payment.Payment
import bunyod.profunctors.effects.{Background, MonadThrow}
import cats.effect.Timer
import cats.implicits._
import io.chrisdavenport.log4cats.Logger
import squants.market.Money
import retry._
import scala.concurrent.duration.DurationInt

final class CheckoutProgram[F[_]: Background: Logger: MonadThrow: Timer](
  paymentClient: PaymentClient[F],
  shoppingCart: ShoppingCart[F],
  orders: Orders[F],
  retryPolicy: RetryPolicy[F]
) {

  def checkout(userId: UserId, card: Card): F[OrderId] =
    shoppingCart
      .get(userId)
      .ensure(EmptyCartError)(_.items.nonEmpty)
      .flatMap {
        case CartTotal(items, total) =>
          for {
            pid <- processPayment(Payment(userId, total, card))
            order <- createOrder(userId, pid, items, total)
            _ <- shoppingCart.delete(userId).attempt.void
          } yield order
      }

  private def processPayment(payment: Payment): F[PaymentId] = {
    val action = retryingOnAllErrors[PaymentId](
      policy = retryPolicy,
      onError = logError("Payment")
    )(paymentClient.process(payment))

    action.adaptError {
      case e =>
        PaymentError(Option(e.getMessage).getOrElse("Unknown"))
    }
  }
  private def createOrder(
    userId: UserId,
    paymentId: PaymentId,
    items: List[cart.CartItem],
    total: Money
  ): F[OrderId] = {
    val action = retryingOnAllErrors[OrderId](
      policy = retryPolicy,
      onError = logError("Order")
    )(orders.create(userId, paymentId, items, total))

    def backgroundAction(fa: F[OrderId]): F[OrderId] =
      fa.adaptError {
          case e => OrderError(e.getMessage)
        }
        .onError {
          case _ =>
            Logger[F].error(s"Failed to create order for Payment: ${paymentId}") *>
              Background[F].schedule(backgroundAction(fa), 1.hour)
        }

    backgroundAction(action)
  }

  private def logError(action: String)(e: Throwable, details: RetryDetails): F[Unit] = ???
}
