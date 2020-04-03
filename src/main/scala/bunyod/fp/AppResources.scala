package bunyod.fp

import bunyod.fp.utils.cfg.Configuration.{Config, HttpClientCfg, PostgreSQLCfg, RedisCfg}
import cats.effect._
import cats.implicits._
import dev.profunktor.redis4cats.algebra.RedisCommands
import dev.profunktor.redis4cats.connection.{RedisClient, RedisURI}
import dev.profunktor.redis4cats.domain.RedisCodec
import dev.profunktor.redis4cats.interpreter.Redis
import dev.profunktor.redis4cats.log4cats._
import io.chrisdavenport.log4cats.Logger
import natchez.Trace.Implicits.noop /// needed for skunk
import org.http4s.client.Client
import org.http4s.client.blaze.BlazeClientBuilder
import scala.concurrent.ExecutionContext
import skunk._

final case class AppResources[F[_]](
  client: Client[F],
  psql: Resource[F, Session[F]],
  redis: RedisCommands[F, String, String]
)

object AppResources {

  def make[F[_]: ConcurrentEffect: ContextShift: Logger](
    cfg: Config
  ): Resource[F, AppResources[F]] = {

    def mkPostgreSqlResource(c: PostgreSQLCfg): SessionPool[F] =
      Session
        .pooled[F](
          host = c.host.value,
          port = c.port.value,
          user = c.user.value,
          database = c.database.value,
          max = c.max.value
        )

    def mkRedisResource(c: RedisCfg): Resource[F, RedisCommands[F, String, String]] =
      for {
        uri <- Resource.liftF(RedisURI.make[F](c.uri.value.value))
        client <- RedisClient[F](uri)
        cmd <- Redis[F, String, String](client, RedisCodec.Utf8)
      } yield cmd

    def mkHttpClient(c: HttpClientCfg): Resource[F, Client[F]] =
      BlazeClientBuilder[F](ExecutionContext.global)
        .withConnectTimeout(c.connectTimeout)
        .withRequestTimeout(c.requestTimeout)
        .resource

    (
      mkHttpClient(cfg.httpClient),
      mkPostgreSqlResource(cfg.postgres),
      mkRedisResource(cfg.redis)
    ).mapN(AppResources.apply[F])

  }

}
