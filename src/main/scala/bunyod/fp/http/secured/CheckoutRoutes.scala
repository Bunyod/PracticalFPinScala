package bunyod.fp.http.secured

import cats.Defer
import cats.effect._
import cats.implicits._
import bunyod.fp.domain.cart.CartPayloads.CartNotFound
import bunyod.fp.domain.checkout.CheckoutService
import bunyod.fp.domain.checkout.CheckoutPayloads.Card
import bunyod.fp.domain.orders.OrdersPayloads._
import bunyod.fp.domain.users.UsersPayloads.CommonUser
import bunyod.fp.http.utils.decoder._
import bunyod.fp.http.utils.json._
import org.http4s._
import org.http4s.circe.JsonDecoder
import org.http4s.dsl.Http4sDsl
import org.http4s.server._

final class CheckoutRoutes[F[_]: Defer: JsonDecoder: MonadThrow](
  program: CheckoutService[F]
) extends Http4sDsl[F] {

  private[http] val prefixPath = "/checkout"

  private val httpRoutes: AuthedRoutes[CommonUser, F] = AuthedRoutes.of { case ar @ POST -> Root as user =>
    ar.req.decodeR[Card] { card =>
      program
        .checkout(user.value.id, card)
        .flatMap(Created(_))
        .recoverWith {
          case CartNotFound(userId) =>
            NotFound(s"Cart not found for user: ${userId.value}")
          case EmptyCartError =>
            BadRequest("Shopping cart is empty!")
          case PaymentError(cause) =>
            BadRequest(cause)
          case OrderError(cause) =>
            BadRequest(cause)
        }
    }

  }

  def routes(authMiddleware: AuthMiddleware[F, CommonUser]): HttpRoutes[F] = Router(
    prefixPath -> authMiddleware(httpRoutes)
  )

}
