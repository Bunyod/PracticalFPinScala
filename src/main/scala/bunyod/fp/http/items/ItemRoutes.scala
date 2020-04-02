package bunyod.fp.http.items

import bunyod.fp.domain.brands.BrandsPayloads._
import bunyod.fp.domain.items._
import cats._
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import bunyod.fp.http.utils.json._
import bunyod.fp.http.utils.refined._

final class ItemRoutes[F[_]: Defer: Monad](
  items: ItemsService[F]
) extends Http4sDsl[F] {

  private[http] val prefixPath = "/items"

  object BrandQueryParam extends OptionalQueryParamDecoderMatcher[BrandParam]("brand")

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root :? BrandQueryParam(brand) =>
      Ok(brand.fold(items.findAll)(b => items.findBy(b.toDomain)))
  }

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )

}
