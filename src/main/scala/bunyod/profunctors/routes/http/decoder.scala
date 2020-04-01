package bunyod.profunctors.routes.http

import cats.implicits._
import bunyod.profunctors.effects.MonadThrow
import io.circe.Decoder
import org.http4s.{Request, Response}
import org.http4s.dsl.Http4sDsl
import org.http4s.circe._

object decoder {

  implicit class RefinedRequestDecoder[F[_]: JsonDecoder: MonadThrow](request: Request[F]) extends Http4sDsl[F] {

    def decodeR[A: Decoder](f: A => F[Response[F]]): F[Response[F]] =
      request.asJsonDecode[A].attempt.flatMap {
        case Left(e) =>
          Option(e.getCause) match {
            case Some(c) if c.getMessage.startsWith("Predicate") => BadRequest(c.getMessage)
            case _ => UnprocessableEntity()
          }
        case Right(a) => f(a)
      }

  }

}
