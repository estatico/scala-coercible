package io.estatico.coercible

/** Standard Coercible instances for the Scala stdlib. */
trait CoercibleStandardInstances {

  /** The R side of a Left can always be cast. */
  implicit def coercibleLeft[L, R1, R2]
    : Coercible[Left[L, R1], Left[L, R2]]
    = Coercible.instance

  /** The L side of a Right can always be cast. */
  implicit def coercibleRight[R, L1, L2]
    : Coercible[Right[L1, R], Right[L2, R]]
    = Coercible.instance

  /** Useful for safely casting cached type class instances for Either. */
  implicit def coercibleFEitherL[F[_[_]], L]
    : Coercible[F[Either[Nothing, ?]], F[Either[L, ?]]]
    = Coercible.instance
}
