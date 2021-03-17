package bunyod.fp

import cats.ApplicativeError

package object effekts {

  type ApThrow[F[_]] = ApplicativeError[F, Throwable]

  object ApThrow {
    def apply[F[_]](implicit env: ApplicativeError[F, Throwable]): ApThrow[F] = env
  }

}
