package bunyod.fp.infrastructure.skunk

import bunyod.fp.domain.auth.AuthPayloads._
import bunyod.fp.domain.crypto.CryptoAlgebra
import bunyod.fp.domain.users.UsersAlgebra
import bunyod.fp.domain.users.UsersPayloads.User
import bunyod.fp.effects._
import bunyod.fp.utils.extensions.Skunkx._
import cats.effect.Resource
import cats.implicits._
import skunk._
import skunk.codec.all._
import skunk.implicits._

class UsersRepository[F[_]: BracketThrow: GenUUID](
  sessionPool: Resource[F, Session[F]],
  crypto: CryptoAlgebra
) extends UsersAlgebra[F] {

  import UsersRepository._

  override def find(
    username: UserName,
    password: Password
  ): F[Option[User]] =
    sessionPool.use { session =>
      session.prepare(selectUser).use { cmd =>
        cmd.option(username).map {
          case Some(u ~ p) if p.value == crypto.encrypt(password) => u.some
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
            .handleErrorWith {
              case SqlState.UniqueViolation(_) =>
                UserNameInUse(username).raiseError[F, UserId]
            }
        }
      }
    }
}

object UsersRepository {

  private val codec: Codec[User ~ EncryptedPassword] =
    (uuid.cimap[UserId] ~ varchar.cimap[UserName] ~ varchar.cimap[EncryptedPassword]).imap {
      case i ~ n ~ p =>
        User(i, n) ~ p
    } {
      case u ~ p =>
        u.id ~ u.name ~ p
    }

  val selectUser: Query[UserName, User ~ EncryptedPassword] =
    sql"""
      SELECT * FROM users
      where name = ${varchar.cimap[UserName]}
      """.query(codec)

  val insertUser: Command[User ~ EncryptedPassword] =
    sql"""
           INSERT INTO users
           VALUES ($codec)
         """.command
}
