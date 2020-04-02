package bunyod.profunctors.effects

import cats.effect.Sync
import cats.implicits._
import io.estatico.newtype.Coercible
import io.estatico.newtype.ops._
import java.util.UUID

trait GenUUID[F[_]] {

  def make: F[UUID]
  def make[A: Coercible[UUID, *]]: F[A]
  def read[A: Coercible[UUID, *]](string: String): F[A]

}

object GenUUID {

  def apply[F[_]](implicit env: GenUUID[F]): GenUUID[F] = env

  implicit def syncGenUUID[F[_]: Sync]: GenUUID[F] = new GenUUID[F] {

    override def make: F[UUID] =
      Sync[F].delay(UUID.randomUUID())

    override def make[A: Coercible[UUID, *]]: F[A] =
      make.map(_.coerce[A])

    override def read[A: Coercible[UUID, *]](str: String): F[A] =
      ApThrow[F].catchNonFatal(UUID.fromString(str).coerce[A])
  }

}
