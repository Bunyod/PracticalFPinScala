package bunyod.fp.itsuite

import cats.effect.IO
import cats.effect.unsafe.implicits.global

object IOAssertion {
  def apply[A](ioa: IO[A]): Unit = ioa.void.unsafeRunSync()
}
