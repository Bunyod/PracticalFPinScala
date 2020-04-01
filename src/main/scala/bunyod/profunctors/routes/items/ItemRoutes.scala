package bunyod.profunctors.routes.items

import bunyod.profunctors.domain.brands.brands._
import bunyod.profunctors.domain.items.Items
import cats._
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import bunyod.profunctors.routes.http.json._
import bunyod.profunctors.routes.http.refined._

final class ItemRoutes[F[_]: Defer: Monad](
  items: Items[F]
) extends Http4sDsl[F] {

  private[routes] val prefixPath = "/items"

  object BrandQueryParam extends OptionalQueryParamDecoderMatcher[BrandParam]("brand")

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root :? BrandQueryParam(brand) =>
      Ok(brand.fold(items.findAll)(b => items.findBy(b.toDomain)))
  }

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )

}
