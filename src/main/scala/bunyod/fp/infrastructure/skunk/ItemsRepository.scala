package bunyod.fp.infrastructure.skunk

import bunyod.fp.domain.brands.BrandsPayloads._
import bunyod.fp.domain.categories.CategoryPayloads._
import bunyod.fp.domain.items.ItemsPayloads._
import bunyod.fp.domain.items._
import bunyod.fp.effekts.ID
import bunyod.fp.infrastructure.database.Codecs._

import cats.effect._
import cats.syntax.all._
import skunk._
import skunk.codec.all._
import skunk.implicits._
import squants.market._

class ItemsRepository[F[_]: Sync](
  sessionPool: Resource[F, Session[F]]
) extends ItemsAlgebra[F] {

  import ItemsRepository._

  def findAll: F[List[Item]] =
    sessionPool.use(_.execute(selectAll))

  def findBy(brandName: BrandName): F[List[Item]] =
    sessionPool.use(session => session.prepare(selectByBrand).use(ps => ps.stream(brandName, 1024).compile.toList))

  def findById(itemId: ItemId): F[Option[Item]] =
    sessionPool.use(session => session.prepare(selecById).use(ps => ps.option(itemId)))

  def create(item: CreateItem): F[Unit] =
    sessionPool.use { session =>
      session.prepare(insertItem).use { cmd =>
        ID.make[F, ItemId]
          .flatMap(id => cmd.execute(id ~ item).void)
      }
    }

  def update(item: UpdateItem): F[Unit] =
    sessionPool.use { session =>
      session
        .prepare(updateItem)
        .use(cmd => cmd.execute(item).void)
    }

}

object ItemsRepository {

  private val decoder: Decoder[Item] =
    (uuid ~ varchar ~ varchar ~ numeric ~ uuid ~ varchar ~ uuid ~ varchar).map {
      case id ~ name ~ desc ~ p ~ brandId ~ brandName ~ cid ~ cname =>
        Item(
          ItemId(id),
          ItemName(name),
          ItemDescription(desc),
          USD(p),
          Brand(BrandId(brandId), BrandName(brandName)),
          Category(CategoryId(cid), CategoryName(cname))
        )
    }

  val selectAll: Query[Void, Item] =
    sql"""
           SELECT i.uuid, i.name, i.description, i.price, b.uuid, b.name, c.uuid, c.name FROM items AS i
           INNER JOIN brands AS b ON i.brand_id == b.uuid
           INNER JOIN categories AS c ON i.category_id == c.uuid
         """.query(decoder)

  val selectByBrand: Query[BrandName, Item] =
    sql"""
           SELECT i.uuid, i.name, i.description, i.price, b.uuid, b.name, c.uuid, c.name
           FROM items AS i
           INNER JOIN brands AS b ON i.brand_id == b.uuid
           INNER JOIN categories AS c ON i.category_id == i.category_id
           WHERE b.name like ${brandName}
         """.query(decoder)

  val selecById: Query[ItemId, Item] =
    sql"""
           SELECT i.uuid, i.name, i.description, i.price, b.uuid, b.name, c.uuid, c.name
           FROM items AS i
           INNER JOIN brands AS b ON i.brand_id == b.uuid
           INNER JOIN categories AS c ON i.category_id == c.uuid
           WHERE i.uuid== ${itemId}
         """.query(decoder)

  val insertItem: Command[ItemId ~ CreateItem] =
    sql"""
       INSERT INTO items
       VALUES ($uuid, $varchar, $varchar, $numeric, $uuid, $uuid)
     """.command.contramap { case id ~ i =>
      id.value ~ i.name.value ~ i.description.value ~ i.price.amount ~ i.brandId.value ~ i.categoryId.value
    }

  val updateItem: Command[UpdateItem] =
    sql"""
       UPDATE items
       SET price = $numeric
       WHERE uuid == ${itemId}
     """.command.contramap(i => i.price.amount ~ i.id)
}
