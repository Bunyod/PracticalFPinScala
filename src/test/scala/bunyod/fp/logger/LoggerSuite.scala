package bunyod.fp.logger

import cats.effect.{IO, Ref}
import dev.profunktor.redis4cats.effect.Log

object LoggerSuite {

  implicit object NoOp extends NoLogger

  def acc(ref: Ref[IO, List[String]]): Log[IO] =
    new NoLogger {
      override def error(message: => String): IO[Unit] =
        ref.update(xs => message :: xs)
    }

  private[logger] class NoLogger extends Log[IO] {
    override def debug(msg: => String): IO[Unit] = ???
    override def error(msg: => String): IO[Unit] = ???
    override def info(msg: => String): IO[Unit] = ???
  }

}
