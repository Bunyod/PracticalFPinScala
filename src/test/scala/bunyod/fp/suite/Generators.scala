package bunyod.fp.suite

import bunyod.fp.domain.auth.AuthPayloads.UserId
import bunyod.fp.domain.brands.BrandsPayloads._
import bunyod.fp.domain.cart.CartPayloads._
import bunyod.fp.domain.categories.CategoryPayloads._
import bunyod.fp.domain.checkout.CheckoutPayloads._
import bunyod.fp.domain.items.ItemsPayloads._
import bunyod.fp.domain.orders.OrdersPayloads._
import eu.timepit.refined.api.Refined
import io.estatico.newtype.Coercible
import io.estatico.newtype.ops._

import java.util.UUID
import org.scalacheck.Gen
import squants.market._

object Generators {

  def cbUuid[A: Coercible[UUID, *]]: Gen[A] =
    Gen.uuid.map(_.coerce[A])

  def cbStr[A: Coercible[String, *]]: Gen[A] =
    genNonEmptyString.map(_.coerce[A])

  def cbInt[A: Coercible[Int, *]]: Gen[A] =
    Gen.posNum[Int].map(_.coerce[A])

  val genMoney: Gen[Money] =
    Gen.posNum[Long].map(n => USD(BigDecimal(n)))

  val genNonEmptyString: Gen[String] =
    Gen
      .chooseNum(21, 40)
      .flatMap(n => Gen.buildableOfN[String, Char](n, Gen.alphaChar))

  val genTuple: Gen[(ItemId, Quantity)] =
    for {
      ii <- cbUuid[ItemId]
      q <- cbInt[Quantity]
    } yield (ii, q)

  val orderGen: Gen[Order] =
    for {
      i <- cbUuid[OrderId]
      p <- cbUuid[PaymentId]
      is <- Gen.nonEmptyMap[ItemId, Quantity](genTuple)
      m <- genMoney
    } yield Order(i, p, is, m)

  val brandGen: Gen[Brand] =
    for {
      i <- cbUuid[BrandId]
      n <- cbStr[BrandName]
    } yield Brand(i, n)

  val categoryGen: Gen[Category] =
    for {
      i <- cbUuid[CategoryId]
      n <- cbStr[CategoryName]
    } yield Category(i, n)

  val itemGen: Gen[Item] =
    for {
      i <- cbUuid[ItemId]
      n <- cbStr[ItemName]
      d <- cbStr[ItemDescription]
      p <- genMoney
      b <- brandGen
      c <- categoryGen
    } yield Item(i, n, d, p, b, c)

  val userIdGen: Gen[UserId] =
    Gen.uuid.map(UserId(_))

  val orderIdGen: Gen[OrderId] =
    Gen.uuid.map(OrderId(_))

  val paymentIdGen: Gen[PaymentId] =
    Gen.uuid.map(PaymentId(_))

  val cartItemGen: Gen[CartItem] =
    for {
      i <- itemGen
      q <- cbInt[Quantity]
    } yield CartItem(i, q)

  val cartTotalGen: Gen[CartTotal] =
    for {
      i <- Gen.nonEmptyListOf(cartItemGen)
      t <- genMoney
    } yield CartTotal(i, t)

  val itemMapGen: Gen[(ItemId, Quantity)] =
    for {
      i <- cbUuid[ItemId]
      q <- cbInt[Quantity]
    } yield i -> q

  val cartGen: Gen[Cart] =
    Gen.nonEmptyMap(itemMapGen).map(Cart.apply)

  val cardGen: Gen[Card] =
    for {
      n <- genNonEmptyString.map[CardNamePred](Refined.unsafeApply)
      u <- Gen.posNum[Long].map[CardNumberPred](Refined.unsafeApply)
      x <- Gen.posNum[Int].map[CardExpirationPred](x => Refined.unsafeApply(x.toString))
      c <- Gen.posNum[Int].map[CardCVVPred](Refined.unsafeApply)
    } yield Card(CardName(n), CardNumber(u), CardExpiration(x), CardCVV(c))

}
