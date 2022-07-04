package bunyod.fp

import bunyod.fp.domain.brands.BrandsService
import bunyod.fp.domain.cart.ShoppingCartService
import bunyod.fp.domain.categories.CategoriesService
import bunyod.fp.domain.checkout.CheckoutService
import bunyod.fp.domain.items.ItemsService
import bunyod.fp.domain.orders.OrdersService
import bunyod.fp.domain.payment.PaymentClientService
import bunyod.fp.http.HttpApi
import bunyod.fp.infrastructure.clients.PaymentClientRepository
import bunyod.fp.infrastructure.redis.ShoppingCartRepository
import bunyod.fp.infrastructure.postgres._
import bunyod.fp.utils.cfg.Configurable
import bunyod.fp.utils.extensions.Security
import cats.effect._
import cats.implicits._
import dev.profunktor.redis4cats.effect.Log
import dev.profunktor.redis4cats.effect.Log.Stdout.instance
import org.http4s.ember.server.EmberServerBuilder
import retry.RetryPolicies._

object MainIO extends IOApp.Simple with Configurable[IO] {

  override def run: IO[Unit] =
    config.load.flatMap { cfg =>
      Log[IO].info(s"Loaded config: $cfg") >>
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
            retryPolicy = limitRetries[IO](cfg.checkout.retriesLimit.value).combine(
              exponentialBackoff[IO](cfg.checkout.retriesBackoff)
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
          } yield routes
        }.flatMap { routes =>
          EmberServerBuilder
            .default[IO]
            .withHost(cfg.httpServer.host)
            .withPort(cfg.httpServer.port)
            .withHttpApp(routes.httpApp)
            .build
            .useForever
        }
    }

}
