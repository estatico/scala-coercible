package io.estatico.coercible.macros

import scala.reflect.macros.blackbox

@macrocompat.bundle
final class CoercibleMacros(val c: blackbox.Context) {

  import c.universe._

  def E = CoercibleMacrosErrorMessages

  def materializeCoercible[A : WeakTypeTag, B : WeakTypeTag]: Tree = {
    val A = weakTypeOf[A]
    val B = weakTypeOf[B]

    if (A.typeSymbol != B.typeSymbol) fail(E.typeMismatch(A, B))
    if (!A.typeSymbol.isClass || !A.typeSymbol.asClass.isFinal) fail(E.nonFinal)

    val unsupportedBaseClasses = A.baseClasses.toSet.diff(allowableBaseClasses + A.typeSymbol)
    if (unsupportedBaseClasses.nonEmpty) fail(E.unsupportedParents(unsupportedBaseClasses))

    if (A.typeSymbol.asClass.primaryConstructor.asMethod.paramLists != List(Nil)) fail(E.constructorArgs)

    if (A.members.exists(s => s.asTerm.isVar || s.asTerm.isVal)) fail(E.varOrValFields)

    q"io.estatico.coercible.Coercible.instance[$A, $B]"
  }

  private def fail(msg: String) = c.abort(c.enclosingPosition, msg)

  private val allowableBaseClasses = Set(
    typeOf[Object],
    typeOf[AnyRef],
    typeOf[Any]
  ).map(_.typeSymbol)
}

object CoercibleMacrosErrorMessages {

  def typeMismatch(A: Any, B: Any) = s"Type mismatch: $A cannot be coerced to $B"

  def nonFinal = "Cannot coerce non-final class"

  def unsupportedParents(classes: TraversableOnce[Any]): String
    = s"Cannot coerce class with parents: " + classes.mkString(",")

  def constructorArgs = "Cannot coerce class with constructor arguments"

  def varOrValFields = "Cannot coerce class with var or val fields"
}
