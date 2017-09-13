package io.estatico.coercible.macros

import scala.reflect.macros.blackbox

@macrocompat.bundle
final class CoercibleMacros(val c: blackbox.Context) {

  import c.universe._

  def materializeCoercible[A : WeakTypeTag, B : WeakTypeTag]: Tree = {
    val A = weakTypeOf[A]
    val B = weakTypeOf[B]

    if (A.typeSymbol != B.typeSymbol) fail(s"$A cannot be coerced to $B")
    if (!A.typeSymbol.isClass || !A.typeSymbol.asClass.isFinal) fail("Only final classes are supported")

    val unsupportedBaseClasses = A.baseClasses.toSet.diff(allowableBaseClasses + A.typeSymbol)
    if (unsupportedBaseClasses.nonEmpty) fail(s"Unsupported base classes: $unsupportedBaseClasses")

    if (A.typeSymbol.asClass.primaryConstructor.asMethod.paramLists != List(Nil)) {
      fail("Only classes with empty constructors are supported")
    }

    if (A.members.exists(s => s.asTerm.isVar || s.asTerm.isVal)) fail("Cannot coerce class with var or val fields")

    q"io.estatico.coercible.Coercible.instance[$A, $B]"
  }

  private def fail(msg: String) = c.abort(c.enclosingPosition, msg)

  private val allowableBaseClasses = Set(
    typeOf[Object],
    typeOf[AnyRef],
    typeOf[Any]
  ).map(_.typeSymbol)
}
