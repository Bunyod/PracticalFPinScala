package bunyod.profunctors.routes.http

import bunyod.profunctors.domain.auth.auth._
import bunyod.profunctors.domain.brands.brands._
import bunyod.profunctors.domain.cart.cart._
import bunyod.profunctors.domain.categories.categories._
import bunyod.profunctors.domain.checkout.checkout._
import bunyod.profunctors.domain.items.items._
import bunyod.profunctors.domain.orders.orders._
import bunyod.profunctors.domain.payment.payment._
import bunyod.profunctors.domain.users.users._
import bunyod.profunctors.routes.http.refined._
import cats.Applicative
import dev.profunktor.auth.jwt.JwtToken
import io.circe._
import io.circe.generic.semiauto._
import io.circe.refined._
import io.estatico.newtype.Coercible
import io.estatico.newtype.ops._
import org.http4s.EntityEncoder
import org.http4s.circe.jsonEncoderOf
import squants.market._

object json extends JsonCodecs {
  implicit def deriveEntityEncoder[F[_]: Applicative, A: Encoder]: EntityEncoder[F, A] = jsonEncoderOf[F, A]
}

private[http] trait JsonCodecs {

  // ----- Overriding some Coercible codecs ----
  implicit val brandParamDecoer: Decoder[BrandParam] =
    Decoder.forProduct1("name")(BrandParam.apply)

  implicit val categoryParamDecoer: Decoder[CategoryParam] =
    Decoder.forProduct1("name")(CategoryParam.apply)

  implicit val paymentIdDecoer: Decoder[PaymentId] =
    Decoder.forProduct1("paymentId")(PaymentId.apply)

  // ----- Coercible codecs -----
  implicit def coercibleDecoder[A: Coercible[B, *], B: Decoder]: Decoder[A] =
    Decoder[B].map(_.coerce[A])

  implicit def coercibleEncoder[A: Coercible[B, *], B: Encoder]: Encoder[A] =
    Encoder[B].contramap(_.repr.asInstanceOf[B])

  implicit def coercibleKeyDecoder[A: Coercible[B, *], B: KeyDecoder]: KeyDecoder[A] =
    KeyDecoder[B].map(_.coerce[A])

  implicit def coercibleKeyEncoder[A: Coercible[B, *], B: KeyEncoder]: KeyEncoder[A] =
    KeyEncoder[B].contramap[A](_.repr.asInstanceOf[B])

  // ----- Domain codecs -----

  implicit val brandEncoder: Encoder[Brand] = deriveEncoder[Brand]
  implicit val brandDecoder: Decoder[Brand] = deriveDecoder[Brand]

  implicit val categoryEncoder: Encoder[Category] = deriveEncoder[Category]
  implicit val categoryDecoder: Decoder[Category] = deriveDecoder[Category]

  implicit val moneyEncoder: Encoder[Money] =
    Encoder[BigDecimal].contramap(_.amount)

  implicit val moneyDecoder: Decoder[Money] =
    Decoder[BigDecimal].map(USD.apply)

  implicit val itemEncoder: Encoder[Item] = deriveEncoder[Item]
  implicit val itemDecoder: Decoder[Item] = deriveDecoder[Item]

  implicit val createItemParamDecoer: Decoder[CreateItemParam] = deriveDecoder[CreateItemParam]
  implicit val updateItemParamDecoer: Decoder[UpdateItemParam] = deriveDecoder[UpdateItemParam]

  implicit val cartItemEncoder: Encoder[CartItem] = deriveEncoder[CartItem]
  implicit val cartItemDecoder: Decoder[CartItem] = deriveDecoder[CartItem]

  implicit val cartTotalEncoder: Encoder[CartTotal] = deriveEncoder[CartTotal]

  implicit val orderEcoder: Encoder[Order] = deriveEncoder[Order]

  implicit val cardDecoder: Decoder[Card] = deriveDecoder[Card]
  implicit val cardEcoder: Encoder[Card] = deriveEncoder[Card]

  implicit val tokenEncoder: Encoder[JwtToken] =
    Encoder.forProduct1("acces_token")(_.value)

  implicit val cartEncoder: Encoder[Cart] =
    Encoder.forProduct1("items")(_.items)

  implicit val cartDecoder: Decoder[Cart] =
    Decoder.forProduct1("items")(Cart.apply)

  implicit val userEncoder: Encoder[User] = deriveEncoder[User]
  implicit val userDecoder: Decoder[User] = deriveDecoder[User]

  implicit val paymentEcoder: Encoder[Payment] = deriveEncoder[Payment]

  implicit val createUserDecoder: Decoder[CreateUser] = deriveDecoder[CreateUser]

  implicit val loginUserDecoder: Decoder[LoginUser] = deriveDecoder[LoginUser]

}
