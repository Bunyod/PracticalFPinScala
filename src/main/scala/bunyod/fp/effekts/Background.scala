package bunyod.fp.effekts

import cats.effect._
import cats.effect.implicits._
import cats.implicits._
import scala.concurrent.duration.FiniteDuration

trait Background[F[_]] {

  def schedule[A](
    fa: F[A],
    duration: FiniteDuration
  ): F[Unit]

}

object Background {
  def apply[F[_]](implicit ev: Background[F]): Background[F] = ev

  implicit def concurrentBackground[F[_]: Temporal]: Background[F] =
    new Background[F] {

      def schedule[A](
        fa: F[A],
        duration: FiniteDuration
      ): F[Unit] =
        (Temporal[F].sleep(duration) *> fa).start.void
    }

}
