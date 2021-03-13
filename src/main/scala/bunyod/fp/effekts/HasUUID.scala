package bunyod.fp.effekts

import cats.Functor
import cats.syntax.all._
import derevo._

import java.util.UUID

trait HasUUID[A] {

  def _UUID: Iso[A, UUID]

  def uuid[F[_]: Functor: GenUUID]: F[A] =
    GenUUID[F].make.map(_UUID.reverse)

  def read[F[_]: Functor: GenUUID](str: String): F[A] =
    GenUUID[F].read(str).map(_UUID.reverse)
}

object HasUUID {
  def apply[A: HasUUID]: HasUUID[A] = implicitly

  implicit val identityUUID: HasUUID[UUID] = new HasUUID[UUID] {
    val _UUID = Iso[UUID, UUID](identity, identity)
  }
}

object uuid extends Derivation[HasUUID] with NewTypeDerivation[HasUUID] {
  def instance[A]: HasUUID[A] = new HasUUID[A] {
    val _UUID: Iso[A, UUID] = Iso[A, UUID](_.asInstanceOf[UUID], _.asInstanceOf[A])
  }
}
