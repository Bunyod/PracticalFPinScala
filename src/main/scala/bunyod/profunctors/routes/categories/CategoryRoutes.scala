package bunyod
package profunctors
package routes
package categories

import domain.categories.CategoriesAlgebra
import cats.{Defer, Monad}
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import routes.http.json._

final class CategoryRoutes[F[_]: Defer: Monad](categories: CategoriesAlgebra[F]) extends Http4sDsl[F] {

  private[routes] val pathPrefix = "/categories"

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root =>
      Ok(categories.findAll)
  }

  val routes: HttpRoutes[F] = Router(
    pathPrefix -> httpRoutes
  )

}
