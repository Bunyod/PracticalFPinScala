package bunyod.profunctors.infrastructure.skunk

import bunyod.profunctors.domain.brands.BrandsPayloads._
import bunyod.profunctors.domain.brands._
import bunyod.profunctors.effects._
import bunyod.profunctors.extensions.Skunkx._
import cats.effect._
import cats.implicits._
import skunk._
import skunk.codec.all._
import skunk.implicits._

class BrandsInterpreter[F[_]: BracketThrow: GenUUID](
  sessionPool: Resource[F, Session[F]]
) extends BrandsAlgebra[F] {

  import BrandQueries._

  def findAll: F[List[Brand]] =
    sessionPool.use(_.execute(selectAll))

  def create(name: BrandName): F[Unit] =
    sessionPool.use { session =>
      session.prepare(insertBrand).use { cmd =>
        GenUUID[F].make[BrandId].flatMap { id =>
          cmd
            .execute(
              Brand(id, name)
            )
            .void
        }
      }
    }

}

object BrandQueries {

  private val codec: Codec[Brand] =
    (uuid.cimap[BrandId] ~ varchar.cimap[BrandName]).imap {
      case i ~ n => Brand(i, n)
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
