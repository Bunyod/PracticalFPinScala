package bunyod.profunctors.domain.checkout

import bunyod.profunctors.domain.auth.AuthPayloads.UserId
import bunyod.profunctors.domain.cart.{CartPayloads, ShoppingCartAlgebra}
import bunyod.profunctors.domain.cart.CartPayloads.CartTotal
import bunyod.profunctors.domain.checkout.CheckoutPayloads.Card
import bunyod.profunctors.domain.orders.OrdersPayloads.{EmptyCartError, OrderError, OrderId, PaymentError, PaymentId}
import bunyod.profunctors.domain.orders.OrdersAlgebra
import bunyod.profunctors.domain.payment.PaymentClientAlgebra
import bunyod.profunctors.domain.payment.PaymentPayloads.Payment
import bunyod.profunctors.effects.{Background, MonadThrow}
import cats.effect.Timer
import cats.implicits._
import io.chrisdavenport.log4cats.Logger
import squants.market.Money
import retry._
import scala.concurrent.duration.DurationInt

final class CheckoutProgram[F[_]: Background: Logger: MonadThrow: Timer](
  paymentClient: PaymentClientAlgebra[F],
  shoppingCart: ShoppingCartAlgebra[F],
  orders: OrdersAlgebra[F],
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
    items: List[CartPayloads.CartItem],
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
