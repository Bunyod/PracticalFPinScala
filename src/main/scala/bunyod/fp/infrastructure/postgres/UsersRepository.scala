package bunyod.fp.infrastructure.postgres

import bunyod.fp.domain.auth.AuthPayloads._
import bunyod.fp.domain.crypto.CryptoAlgebra
import bunyod.fp.domain.users.UsersAlgebra
import bunyod.fp.domain.users.UsersPayloads.User
import bunyod.fp.effekts.GenUUID
import bunyod.fp.utils.extensions.Skunkx._
import cats.Functor
import cats.effect._
import cats.syntax.all._
import skunk._
import skunk.codec.all._
import skunk.implicits._

class UsersRepository[F[_]: BracketThrow: GenUUID: Functor](
  sessionPool: Resource[F, Session[F]],
  crypto: CryptoAlgebra
) extends UsersAlgebra[F] {

  import UsersRepository._

  override def find(username: UserName, password: Password): F[Option[User]] =
    sessionPool.use { session =>
      session.prepare(selectUser).use { cmd =>
        cmd.option(username).map {
          case Some(u ~ p) if p.value == crypto.encrypt(password).value => u.some
          case _ => none[User]
        }
      }
    }

  override def create(username: UserName, password: Password): F[UserId] =
    sessionPool.use { session =>
      session.prepare(insertUser).use { cmd =>
        GenUUID[F].make[UserId].flatMap { id =>
          cmd
            .execute(User(id, username) ~ crypto.encrypt(password))
            .as(id)
            .handleErrorWith { case SqlState.UniqueViolation(_) =>
              UserNameInUse(username).raiseError[F, UserId]
            }
        }
      }
    }
}

object LiveUsersRepository {
  def make[F[_]: Sync](
    sessionPool: Resource[F, Session[F]],
    cryptoAlgebra: CryptoAlgebra
  ): F[UsersAlgebra[F]] =
    Sync[F].delay(
      new UsersRepository[F](sessionPool, cryptoAlgebra)
    )
}

object UsersRepository {

  val codec: Codec[User ~ EncryptedPassword] =
    (uuid.cimap[UserId] ~ varchar.cimap[UserName] ~ varchar.cimap[EncryptedPassword])
      .imap { case i ~ n ~ p =>
        User(i, n) ~ p
      } { case u ~ p =>
        u.id ~ u.name ~ p
      }

  val selectUser: Query[UserName, User ~ EncryptedPassword] =
    sql"""
        SELECT * FROM users
        WHERE name = ${varchar.cimap[UserName]}
       """.query(codec)

  val insertUser: Command[User ~ EncryptedPassword] =
    sql"""
        INSERT INTO users
        VALUES ($codec)
        """.command
}
