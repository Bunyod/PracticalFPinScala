package bunyod.fp.infrastructure.database

import bunyod.fp.domain.auth.AuthPayloads._
import bunyod.fp.domain.brands.BrandsPayloads._
import bunyod.fp.domain.categories.CategoryPayloads._
import bunyod.fp.domain.items.ItemsPayloads.ItemId
import bunyod.fp.domain.orders.OrdersPayloads._

import skunk._
import skunk.codec.all._

object Codecs {
  val brandId: Codec[BrandId] = uuid.imap[BrandId](BrandId(_))(_.value)
  val brandName: Codec[BrandName] = varchar.imap[BrandName](BrandName(_))(_.value)

  val categoryId: Codec[CategoryId] = uuid.imap[CategoryId](CategoryId(_))(_.value)
  val categoryName: Codec[CategoryName] = varchar.imap[CategoryName](CategoryName(_))(_.value)

  val itemId: Codec[ItemId] = uuid.imap[ItemId](ItemId(_))(_.value)

  val orderId: Codec[OrderId] = uuid.imap[OrderId](OrderId(_))(_.value)
  val paymentId: Codec[PaymentId] = uuid.imap[PaymentId](PaymentId(_))(_.value)

  val userId: Codec[UserId] = uuid.imap[UserId](UserId(_))(_.value)
  val userName: Codec[UserName] = varchar.imap[UserName](UserName(_))(_.value)

  val encPassword: Codec[EncryptedPassword] = varchar.imap[EncryptedPassword](EncryptedPassword(_))(_.value)
}
