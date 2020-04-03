package bunyod.fp

import bunyod.fp.domain.brands.BrandsService
import bunyod.fp.domain.cart.ShoppingCartService
import bunyod.fp.domain.categories.CategoriesService
import bunyod.fp.domain.checkout.CheckoutService
import bunyod.fp.domain.items.ItemsService
import bunyod.fp.domain.orders.OrdersService
import bunyod.fp.domain.payment.PaymentClientService
import bunyod.fp.http.shop.modules.HttpApi
import bunyod.fp.infrastructure.clients.PaymentClientRepository
import bunyod.fp.infrastructure.redis.ShoppingCartRepository
import bunyod.fp.infrastructure.skunk._
import bunyod.fp.utils.cfg.Configurable
import bunyod.fp.utils.extensions.Security
import cats.effect._
import cats.implicits._
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import org.http4s.server.blaze.BlazeServerBuilder
import retry.RetryPolicies.{exponentialBackoff, limitRetries}
import retry.RetryPolicy

object MainIO extends IOApp with Configurable {

  implicit val logger = Slf4jLogger.getLogger[IO]

  override def run(args: List[String]): IO[ExitCode] =
    config.load[IO].flatMap { cfg =>
      Logger[IO].info(s"Loaded config: $cfg") *>
        AppResources.make[IO](cfg).use { res =>
          for {
            security <- Security.make[IO](cfg, res.psql, res.redis)
            paymentRepo = new PaymentClientRepository[IO](cfg.payment, res.client)
            paymentService = new PaymentClientService[IO](paymentRepo)
            itemsRepo = new ItemsRepository[IO](res.psql)
            itemsService = new ItemsService[IO](itemsRepo)
            shoppingCartRepo = new ShoppingCartRepository[IO](itemsRepo, res.redis, cfg.shoppingCart)
            shoppingCartService = new ShoppingCartService[IO](shoppingCartRepo)
            orderRepo = new OrdersRepository[IO](res.psql)
            orderService = new OrdersService[IO](orderRepo)
            brandsRepo = new BrandsRepository[IO](res.psql)
            brandsService = new BrandsService[IO](brandsRepo)
            categoryRepo = new CategoriesRepository[IO](res.psql)
            categoryService = new CategoriesService[IO](categoryRepo)
            retryPolicy: RetryPolicy[IO] = limitRetries[IO](cfg.checkout.retriesLimit.value) |+| exponentialBackoff[IO](
              cfg.checkout.retriesBackoff
            )
            checkoutService = new CheckoutService[IO](paymentService, shoppingCartService, orderService, retryPolicy)
            routes = new HttpApi[IO](
              brandsService,
              categoryService,
              itemsService,
              shoppingCartService,
              checkoutService,
              orderService,
              security
            )
            _ <- BlazeServerBuilder[IO]
              .bindHttp(
                cfg.httpServer.port.value,
                cfg.httpServer.host.value
              )
              .withHttpApp(routes.httpApp)
              .serve
              .compile
              .drain

          } yield ExitCode.Success
        }
    }

}
