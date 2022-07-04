package bunyod.fp.http

import bunyod.fp.domain.brands.BrandsService
import bunyod.fp.domain.cart.ShoppingCartService
import bunyod.fp.domain.categories.CategoriesService
import bunyod.fp.domain.checkout.CheckoutService
import bunyod.fp.domain.items.ItemsService
import bunyod.fp.domain.orders.OrdersService
import bunyod.fp.domain.users.UsersPayloads._
import bunyod.fp.http.admin._
import bunyod.fp.http.auth._
import bunyod.fp.http.brands.BrandRoutes
import bunyod.fp.http.categories.CategoryRoutes
import bunyod.fp.http.secured.CheckoutRoutes
import bunyod.fp.http.items.ItemRoutes
import bunyod.fp.http.secured._
import bunyod.fp.utils.extensions.Security
import cats.effect._
import cats.implicits._
import dev.profunktor.auth.JwtAuthMiddleware
import org.http4s._
import org.http4s.implicits._
import org.http4s.server.middleware._
import org.http4s.server.Router

import scala.concurrent.duration._

final class HttpApi[F[_]: Async](
  brandsService: BrandsService[F],
  categoryService: CategoriesService[F],
  itemsService: ItemsService[F],
  cartService: ShoppingCartService[F],
  checkoutService: CheckoutService[F],
  orderService: OrdersService[F],
  security: Security[F]
) {

  private val adminMiddleware =
    JwtAuthMiddleware[F, AdminUser](security.adminJwtAuth.value, security.adminsAuthService.findUser)

  private val usersMiddleware =
    JwtAuthMiddleware[F, CommonUser](security.userJwtAuth.value, security.usersAuthService.findUser)

  // Auth routes
  private val loginRoutes = new LoginRoutes[F](security.authService).routes
  private val logoutRoutes = new LogoutRoutes[F](security.authService).routes(usersMiddleware)
  private val userRoutes = new UserRoutes[F](security.authService).routes

  // Open routes
  private val brandRoutes = new BrandRoutes[F](brandsService).routes
  private val categoryRoutes = new CategoryRoutes[F](categoryService).routes
  private val itemRoutes = new ItemRoutes[F](itemsService).routes

  // Secured routes
  private val cartRoutes = new CartRoutes[F](cartService).routes(usersMiddleware)
  private val checkoutRoutes = new CheckoutRoutes[F](checkoutService).routes(usersMiddleware)
  private val orderRoutes = new OrderRoutes[F](orderService).routes(usersMiddleware)

  // Admin routes
  private val adminBrandRoutes = new AdminBrandRoutes[F](brandsService).routes(adminMiddleware)
  private val adminCategoryRoutes = new AdminCategoryRoutes[F](categoryService).routes(adminMiddleware)
  private val adminItemRoutes = new AdminItemRoutes[F](itemsService).routes(adminMiddleware)

  // Combining all the http routes
  private val openRoutes: HttpRoutes[F] =
    itemRoutes <+> brandRoutes <+>
      categoryRoutes <+> loginRoutes <+> userRoutes <+>
      logoutRoutes <+> cartRoutes <+> orderRoutes <+>
      checkoutRoutes

  private val adminRoutes: HttpRoutes[F] =
    adminItemRoutes <+> adminBrandRoutes <+> adminCategoryRoutes

  private val routes: HttpRoutes[F] = Router(
    version.v1 -> openRoutes,
    version.v2 + "/admin" -> adminRoutes
  )

  private val middleware: HttpRoutes[F] => HttpRoutes[F] = { http: HttpRoutes[F] => AutoSlash(http) }
    .andThen { http: HttpRoutes[F] => CORS.policy.withAllowOriginAll.apply(http) }
    .andThen { http: HttpRoutes[F] => Timeout(60.seconds)(http) }

  private val loggers: HttpApp[F] => HttpApp[F] = { http: HttpApp[F] => RequestLogger.httpApp(true, true)(http) }
    .andThen { http: HttpApp[F] =>
      ResponseLogger.httpApp(true, true)(http)
    }

  val httpApp: HttpApp[F] = loggers(middleware(routes).orNotFound)

}

object version {
  val v1 = "/v1"
  val v2 = "/v2"
}
