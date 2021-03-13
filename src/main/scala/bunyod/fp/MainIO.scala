package bunyod.fp

import bunyod.fp.domain.brands.BrandsService
import bunyod.fp.domain.cart.ShoppingCartService
import bunyod.fp.domain.categories.CategoriesService
import bunyod.fp.domain.checkout.CheckoutService
import bunyod.fp.domain.items.ItemsService
import bunyod.fp.domain.orders.OrdersService
import bunyod.fp.http.{HttpApi, HttpClients}
import bunyod.fp.infrastructure.redis.ShoppingCartRepository
import bunyod.fp.infrastructure.skunk._
import bunyod.fp.utils.cfg.Configurable
import bunyod.fp.utils.extensions.Security
import retry.RetryPolicies.{exponentialBackoff, limitRetries}
import retry.RetryPolicy
import cats.effect._
import cats.syntax.all._
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import org.http4s.ember.server.EmberServerBuilder

object MainIO extends IOApp with Configurable {

  implicit val logger = Slf4jLogger.getLogger[IO]

  override def run(args: List[String]): IO[ExitCode] =
    config.load[IO].flatMap { cfg =>
      Logger[IO].info(s"Loaded config: $cfg") *>
        AppResources
          .make[IO](cfg)
          .evalMap { res =>
            Security.make[IO](cfg, res.psql, res.redis).map { security =>
              val clients = HttpClients.make[IO](cfg.payment, res.client)
              val itemsRepo = new ItemsRepository[IO](res.psql)
              val itemsService = new ItemsService[IO](itemsRepo)
              val shoppingCartRepo = new ShoppingCartRepository[IO](itemsRepo, res.redis, cfg.shoppingCart)
              val shoppingCartService = new ShoppingCartService[IO](shoppingCartRepo)
              val orderRepo = new OrdersRepository[IO](res.psql)
              val orderService = new OrdersService[IO](orderRepo)
              val brandsRepo = new BrandsRepository[IO](res.psql)
              val brandsService = new BrandsService[IO](brandsRepo)
              val categoryRepo = new CategoriesRepository[IO](res.psql)
              val categoryService = new CategoriesService[IO](categoryRepo)
              val retryPolicy: RetryPolicy[IO] = limitRetries[IO](
                cfg.checkout.retriesLimit.value
              ) |+| exponentialBackoff[IO](cfg.checkout.retriesBackoff)
              val checkoutService =
                new CheckoutService[IO](clients.payment, shoppingCartService, orderService, retryPolicy)
              val api = new HttpApi[IO](
                brandsService,
                categoryService,
                itemsService,
                shoppingCartService,
                checkoutService,
                orderService,
                security
              )
              cfg.httpServer -> api

            }
          }
          .flatMap { case (cfg, api) =>
            EmberServerBuilder
              .default[IO]
              .withHost(cfg.host.value)
              .withPort(cfg.port.value)
              .withHttpApp(api.httpApp)
              .build

          }
          .use { server =>
            Logger[IO].info(s"HTTP Server started at ${server.address}") >>
              IO.never.as(ExitCode.Success)
          }
    }

}
