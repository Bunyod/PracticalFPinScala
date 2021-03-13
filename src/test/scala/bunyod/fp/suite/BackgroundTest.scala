package bunyod.fp.suite

import bunyod.fp.effekts.Background
import cats.effect._
import cats.effect.concurrent.Ref
import scala.concurrent.duration.FiniteDuration

object BackgroundTest {

  val NoOp: Background[IO] = new Background[IO] {
    def schedule[A](fa: IO[A], duration: FiniteDuration): IO[Unit] = IO.unit
  }

  def counter(ref: Ref[IO, Int]): Background[IO] =
    new Background[IO] {
      def schedule[A](fa: IO[A], duration: FiniteDuration): IO[Unit] =
        ref.update(_ + 1)
    }

}
