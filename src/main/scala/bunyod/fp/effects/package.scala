package bunyod.fp

import cats.effect.Bracket
import cats.{ApplicativeError, MonadError}
import io.estatico.newtype.macros.newtype
import scala.concurrent.duration.FiniteDuration

package object effects {

  type MonadThrow[F[_]] = MonadError[F, Throwable]

  object MonadThrow {
    def apply[F[_]](implicit ev: MonadError[F, Throwable]): MonadThrow[F] = ev
  }

  type ApThrow[F[_]] = ApplicativeError[F, Throwable]

  object ApThrow {
    def apply[F[_]](implicit env: ApplicativeError[F, Throwable]): ApThrow[F] = env
  }


  type BracketThrow[F[_]] = Bracket[F, Throwable]

  object BracketThrow {
    def apply[F[_]](implicit env: Bracket[F, Throwable]): Bracket[F, Throwable] = env
  }

  @newtype case class TokenExpiration(value: FiniteDuration)
  @newtype case class ShoppingCartExpiration(value: FiniteDuration)

}
