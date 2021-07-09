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
import bunyod.fp.infrastructure.postgres._
import bunyod.fp.infrastructure.redis.ShoppingCartRepository
import bunyod.fp.utils.cfg.Configurable
import bunyod.fp.utils.extensions.Security
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import org.http4s.server.blaze.BlazeServerBuilder
import retry.RetryPolicies._
import retry.RetryPolicy
import cats.syntax.semigroup._
import zio._
import zio.interop.catz._
import zio.interop.catz.implicits._

import scala.concurrent.ExecutionContext

object MainZIO extends CatsApp with Configurable {

  implicit val logger = Slf4jLogger.getLogger[Task]

  override def run(args: List[String]): UIO[Int] =
    config
      .load[Task]
      .flatMap { cfg =>
        Logger[Task].info(s"Loaded config: $cfg") *>
          AppResources.make[Task](cfg).use { res =>
            for {
              security <- Security.make[Task](cfg, res.psql, res.redis)
              paymentRepo = new PaymentClientRepository[Task](cfg.payment, res.client)
              paymentService = new PaymentClientService[Task](paymentRepo)
              itemsRepo = new ItemsRepository[Task](res.psql)
              itemsService = new ItemsService[Task](itemsRepo)
              shoppingCartRepo = new ShoppingCartRepository[Task](itemsRepo, res.redis, cfg.shoppingCart)
              shoppingCartService = new ShoppingCartService[Task](shoppingCartRepo)
              orderRepo = new OrdersRepository[Task](res.psql)
              orderService = new OrdersService[Task](orderRepo)
              brandsRepo = new BrandsRepository[Task](res.psql)
              brandsService = new BrandsService[Task](brandsRepo)
              categoryRepo = new CategoriesRepository[Task](res.psql)
              categoryService = new CategoriesService[Task](categoryRepo)
              retryPolicy: RetryPolicy[Task] = limitRetries[Task](
                cfg.checkout.retriesLimit.value
              ) |+| exponentialBackoff[Task](
                cfg.checkout.retriesBackoff
              )
              checkoutService = new CheckoutService[Task](
                paymentService,
                shoppingCartService,
                orderService,
                retryPolicy
              )
              routes = new HttpApi[Task](
                brandsService,
                categoryService,
                itemsService,
                shoppingCartService,
                checkoutService,
                orderService,
                security
              )
              _ <- BlazeServerBuilder[Task](ExecutionContext.global)
                .bindHttp(
                  cfg.httpServer.port.value,
                  cfg.httpServer.host.value
                )
                .withHttpApp(routes.httpApp)
                .serve
                .compile
                .drain
            } yield 0
          }
      }
      .orDie

}
