package bunyod.profunctors.infrastructure.skunk

import bunyod.profunctors.domain.categories.CategoryPayloads._
import bunyod.profunctors.domain.categories._
import bunyod.profunctors.effects._
import bunyod.profunctors.extensions.Skunkx._
import cats.effect._
import cats.implicits._
import skunk._
import skunk.codec.all._
import skunk.implicits._

class CategoriesInterpreter[F[_]: BracketThrow: GenUUID](
  sessionPool: Resource[F, Session[F]]
) extends CategoriesAlgebra[F] {

  import CategoriesInterpreter._

  def findAll: F[List[Category]] =
    sessionPool.use(_.execute(selectAll))

  def create(categoryName: CategoryName): F[Unit] =
    sessionPool.use { session =>
      session.prepare(insertCategory).use { cmd =>
        GenUUID[F].make[CategoryId].flatMap { id =>
          cmd
            .execute(Category(id, categoryName))
            .void
        }
      }
    }

}

object CategoriesInterpreter {

  private val codec: Codec[Category] =
    (uuid.cimap[CategoryId] ~ varchar.cimap[CategoryName]).imap {
      case i ~ n => Category(i, n)
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
