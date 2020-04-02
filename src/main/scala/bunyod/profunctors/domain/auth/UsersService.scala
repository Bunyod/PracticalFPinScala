package bunyod.profunctors.domain.auth

import dev.profunktor.auth.jwt.JwtToken
import pdi.jwt.JwtClaim

class UsersService[F[_], A](usersAuthRepo: UserAuthAlgebra[F, A]) {

    def findUser(jwtToken: JwtToken, claim: JwtClaim): F[Option[A]] =
        usersAuthRepo.findUser(jwtToken)(claim)

}
