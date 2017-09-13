package io.estatico.coercible

trait CoercibleDerivedInstances {
  implicit def materializeCoercible[A, B]: Coercible[A, B]
    = macro macros.CoercibleMacros.materializeCoercible[A, B]
}
