package bunyod.fp.infrastructure.postgres

import bunyod.fp.domain.brands.BrandsPayloads._
import bunyod.fp.domain.categories.CategoryPayloads._
import bunyod.fp.domain.items.ItemsPayloads._
import bunyod.fp.domain.items._
import bunyod.fp.effekts.GenUUID
import cats.effect._
import cats.syntax.all._
import skunk._
import skunk.codec.all._
import skunk.implicits._
import squants.market._

import java.util.UUID

class ItemsRepository[F[_]: Sync: GenUUID](
  sessionPool: Resource[F, Session[F]]
) extends ItemsAlgebra[F] {

  import ItemsRepository._

  def findAll: F[List[Item]] =
    sessionPool.use(_.execute(selectAll))

  def findBy(brandName: BrandName): F[List[Item]] =
    sessionPool.use(session => session.prepare(selectByBrand).use(ps => ps.stream(brandName.value, 1024).compile.toList))

  def findById(itemId: ItemId): F[Option[Item]] =
    sessionPool.use(session => session.prepare(selectById).use(ps => ps.option(itemId.value)))

  def create(item: CreateItem): F[Unit] =
    sessionPool.use { session =>
      session.prepare(insertItem).use { cmd =>
        GenUUID[F]
          .make
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

object LiveItemsRepository {
  def make[F[_]: Sync](
    sessionPool: Resource[F, Session[F]]
  ): F[ItemsAlgebra[F]] =
    Sync[F].delay(
      new ItemsRepository[F](sessionPool)
    )
}

object ItemsRepository {

  val decoder: Decoder[Item] =
    (uuid ~ varchar ~ varchar ~ numeric ~ uuid ~ varchar ~ uuid ~ varchar).map {
      case i ~ n ~ d ~ p ~ bi ~ bn ~ ci ~ cn =>
        Item(
          ItemId(i),
          ItemName(n),
          ItemDescription(d),
          USD(p),
          Brand(BrandId(bi), BrandName(bn)),
          Category(CategoryId(ci), CategoryName(cn))
        )
    }

  val selectAll: Query[Void, Item] =
    sql"""
        SELECT i.uuid, i.name, i.description, i.price, b.uuid, b.name, c.uuid, c.name
        FROM items AS i
        INNER JOIN brands AS b ON i.brand_id = b.uuid
        INNER JOIN categories AS c ON i.category_id = c.uuid
       """.query(decoder)

  val selectByBrand: Query[String, Item] =
    sql"""
        SELECT i.uuid, i.name, i.description, i.price, b.uuid, b.name, c.uuid, c.name
        FROM items AS i
        INNER JOIN brands AS b ON i.brand_id = b.uuid
        INNER JOIN categories AS c ON i.category_id = c.uuid
        WHERE b.name LIKE $varchar
       """.query(decoder)

  val selectById: Query[UUID, Item] =
    sql"""
        SELECT i.uuid, i.name, i.description, i.price, b.uuid, b.name, c.uuid, c.name
        FROM items AS i
        INNER JOIN brands AS b ON i.brand_id = b.uuid
        INNER JOIN categories AS c ON i.category_id = c.uuid
        WHERE i.uuid = $uuid
       """.query(decoder)

  val insertItem: Command[UUID ~ CreateItem] =
    sql"""
        INSERT INTO items
        VALUES ($uuid, $varchar, $varchar, $numeric, $uuid, $uuid)
       """.command.contramap { case id ~ i =>
      id ~ i.name.value ~ i.description.value ~ i.price.amount ~ i.brandId.value ~ i.categoryId.value
    }

  val updateItem: Command[UpdateItem] =
    sql"""
        UPDATE items
        SET price = $numeric
        WHERE uuid = $uuid
       """.command.contramap(i => i.price.amount ~ i.id.value)

}
