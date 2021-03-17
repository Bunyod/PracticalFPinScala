package bunyod.fp.infrastructure.postgres

import bunyod.fp.domain.brands.BrandsPayloads._
import bunyod.fp.domain.categories.CategoryPayloads._
import bunyod.fp.domain.items.ItemsPayloads._
import bunyod.fp.domain.items._
import bunyod.fp.effekts.GenUUID
import bunyod.fp.utils.extensions.Skunkx._
import cats.effect._
import cats.syntax.all._
import skunk._
import skunk.codec.all._
import skunk.implicits._
import squants.market._

class ItemsRepository[F[_]: Sync: BracketThrow: GenUUID](
  sessionPool: Resource[F, Session[F]]
) extends ItemsAlgebra[F] {

  import ItemsRepository._

  def findAll: F[List[Item]] =
    sessionPool.use(_.execute(selectAll))

  def findBy(brandName: BrandName): F[List[Item]] =
    sessionPool.use(session => session.prepare(selectByBrand).use(ps => ps.stream(brandName, 1024).compile.toList))

  def findById(itemId: ItemId): F[Option[Item]] =
    sessionPool.use(session => session.prepare(selectById).use(ps => ps.option(itemId)))

  def create(item: CreateItem): F[Unit] =
    sessionPool.use { session =>
      session.prepare(insertItem).use { cmd =>
        GenUUID[F]
          .make[ItemId]
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
      new LiveItemsRepository[F](sessionPool)
    )
}

final class LiveItemsRepository[F[_]: Sync] private (
  sessionPool: Resource[F, Session[F]]
) extends ItemsAlgebra[F] {
  import ItemsRepository._

  override def findAll: F[List[Item]] = sessionPool.use(_.execute(selectAll))

  override def findBy(brand: BrandName): F[List[Item]] = sessionPool.use { session =>
    session.prepare(selectByBrand).use { cmd =>
      cmd.stream(brand, 1024).compile.toList
    }
  }

  override def findById(itemId: ItemId): F[Option[Item]] =
    sessionPool.use { session =>
      session.prepare(selectById).use { cmd =>
        cmd.option(itemId)
      }
    }

  override def create(item: CreateItem): F[Unit] =
    sessionPool.use { session =>
      session.prepare(insertItem).use { cmd =>
        GenUUID[F].make[ItemId].flatMap { id =>
          cmd.execute(id ~ item).void
        }
      }
    }

  override def update(item: UpdateItem): F[Unit] =
    sessionPool.use { session =>
      session.prepare(updateItem).use { cmd =>
        cmd.execute(item).void
      }
    }
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

  val selectByBrand: Query[BrandName, Item] =
    sql"""
        SELECT i.uuid, i.name, i.description, i.price, b.uuid, b.name, c.uuid, c.name
        FROM items AS i
        INNER JOIN brands AS b ON i.brand_id = b.uuid
        INNER JOIN categories AS c ON i.category_id = c.uuid
        WHERE b.name LIKE ${varchar.cimap[BrandName]}
       """.query(decoder)

  val selectById: Query[ItemId, Item] =
    sql"""
        SELECT i.uuid, i.name, i.description, i.price, b.uuid, b.name, c.uuid, c.name
        FROM items AS i
        INNER JOIN brands AS b ON i.brand_id = b.uuid
        INNER JOIN categories AS c ON i.category_id = c.uuid
        WHERE i.uuid = ${uuid.cimap[ItemId]}
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
        WHERE uuid = ${uuid.cimap[ItemId]}
       """.command.contramap(i => i.price.amount ~ i.id)

}
