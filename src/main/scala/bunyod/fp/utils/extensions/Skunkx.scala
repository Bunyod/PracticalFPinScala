package bunyod.fp.utils.extensions

import io.estatico.newtype.Coercible
import io.estatico.newtype.ops._
import skunk.Codec

object Skunkx {

  implicit class CodecOps[B](codec: Codec[B]) {
    def cimap[A: Coercible[B, *]](implicit ev: Coercible[A, B]): Codec[A] =
      codec.imap(_.coerce[A])((ev(_)))
  }

}
