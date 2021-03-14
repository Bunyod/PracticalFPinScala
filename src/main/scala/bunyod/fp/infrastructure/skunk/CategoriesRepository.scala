package bunyod.fp.infrastructure.skunk

import bunyod.fp.domain.categories.CategoryPayloads._
import bunyod.fp.domain.categories._
import bunyod.fp.effekts.GenUUID
//import bunyod.fp.effekts._
import bunyod.fp.utils.extensions.Skunkx._
import cats.effect._
import cats.syntax.all._
import skunk._
import skunk.codec.all._
import skunk.implicits._

class CategoriesRepository[F[_]: BracketThrow: GenUUID](
  sessionPool: Resource[F, Session[F]]
) extends CategoriesAlgebra[F] {

  import CategoriesRepository._

  def findAll: F[List[Category]] =
    sessionPool.use(_.execute(selectAll))

  def create(name: CategoryName): F[Unit] =
    sessionPool.use { session =>
      session.prepare(insertCategory).use { cmd =>
        GenUUID[F].make[CategoryId].flatMap { id =>
          cmd.execute(Category(id, name)).void
        }
      }
    }
}

object CategoriesRepository {

  private val codec: Codec[Category] =
    (uuid.cimap[CategoryId] ~ varchar.cimap[CategoryName]).imap { case i ~ n =>
      Category(i, n)
    }(c => c.uuid ~ c.name)

  val selectAll: Query[Void, Category] =
    sql"""
           SELECT * FROM categories
         """.query(codec)

  val insertCategory: Command[Category] =
    sql"""
           INSERT INTO categories
           VALUES ($codec)
         """.command
}
