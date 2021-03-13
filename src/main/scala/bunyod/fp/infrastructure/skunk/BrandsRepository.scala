package bunyod.fp.infrastructure.skunk

import bunyod.fp.domain.brands.BrandsPayloads._
import bunyod.fp.domain.brands._
import bunyod.fp.effekts.{GenUUID, ID}
import bunyod.fp.infrastructure.database.Codecs._
import cats.effect._
import cats.syntax.all._
import skunk._
import skunk.implicits._

class BrandsRepository[F[_]: Sync](
  sessionPool: Resource[F, Session[F]]
) extends BrandsAlgebra[F] {

  import BrandQueries._

  def findAll: F[List[Brand]] =
    sessionPool.use(_.execute(selectAll))

  def create(name: BrandName): F[Unit] =
    sessionPool.use { session =>
      session.prepare(insertBrand).use { cmd =>
        ID.make[F, BrandId].flatMap { id =>
          cmd.execute(Brand(id, name)).void
        }
      }
    }

}

object BrandQueries {

  private val codec: Codec[Brand] =
    (brandId ~ brandName).imap { case i ~ n =>
      Brand(i, n)
    }(b => b.uuid ~ b.name)

  val selectAll: Query[Void, Brand] =
    sql"""
      SELECT * FROM brands
    """.query(codec)

  val insertBrand: Command[Brand] =
    sql"""
     INSERT INTO brands  VALUES ($codec)
    """.command

}
