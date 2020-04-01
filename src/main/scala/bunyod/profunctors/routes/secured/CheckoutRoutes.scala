package bunyod.profunctors.routes.checkout

import cats.Defer
import cats.implicits._
import bunyod.profunctors.domain.cart.CartPayloads.CartNotFound
import bunyod.profunctors.domain.checkout.CheckoutProgram
import bunyod.profunctors.domain.checkout.CheckoutPayloads.Card
import bunyod.profunctors.domain.orders.OrdersPayloads.{EmptyCartError, OrderError, PaymentError}
import bunyod.profunctors.domain.users.UsersPayloads.CommonUser
import bunyod.profunctors.effects.MonadThrow
import bunyod.profunctors.routes.http.decoder._
import bunyod.profunctors.routes.http.json._
import org.http4s._
import org.http4s.circe.JsonDecoder
import org.http4s.dsl.Http4sDsl
import org.http4s.server._

final class CheckoutRoutes[F[_]: Defer: JsonDecoder: MonadThrow](
  program: CheckoutProgram[F]
) extends Http4sDsl[F] {

  private[routes] val prefixPath = "/checkout"

  private val httpRoutes: AuthedRoutes[CommonUser, F] = AuthedRoutes.of {

    case ar @ POST -> Root as user =>
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
