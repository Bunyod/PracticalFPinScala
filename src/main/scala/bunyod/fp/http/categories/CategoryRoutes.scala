package bunyod.fp.http.categories

import bunyod.fp.domain.categories._
//import bunyod.fp.http.utils.json._
import cats.{Defer, Monad}
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router

final class CategoryRoutes[F[_]: Defer: Monad](categories: CategoriesService[F]) extends Http4sDsl[F] {

  private[http] val pathPrefix = "/categories"

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] { case GET -> Root =>
    Ok(categories.findAll)
  }

  val routes: HttpRoutes[F] = Router(
    pathPrefix -> httpRoutes
  )

}
