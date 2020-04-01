package bunyod.profunctors.routes.http

import cats.implicits._
import eu.timepit.refined._
import eu.timepit.refined.api._
import eu.timepit.refined.collection.Size
import io.estatico.newtype.Coercible
import io.estatico.newtype.ops._
import org.http4s.{ParseFailure, QueryParamDecoder}

object refined {

  implicit def coercibleQueryParamDecoder[A: Coercible[B, *], B: QueryParamDecoder]: QueryParamDecoder[A] =
    QueryParamDecoder[B].map(_.coerce[A])

  implicit def refinedParamDecoder[T: QueryParamDecoder, P](
    implicit env: Validate[T, P]
  ): QueryParamDecoder[T Refined P] =
    QueryParamDecoder[T].emap(
      refineV[P](_).leftMap(m => ParseFailure(m, m))
    )

  implicit def validateSizeN[N <: Int, R](implicit w: ValueOf[N]): Validate.Plain[R, Size[N]] =
    Validate.fromPredicate[R, Size[N]](
      _.toString.size == w.value,
      _ => s"Must have ${w.value} digits",
      Size[N](w.value)
    )

}
