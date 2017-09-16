package io.estatico.coercible

import io.estatico.coercible
import org.scalatest.FlatSpec
import org.scalatest.exceptions.TestFailedException
import org.scalatest.prop.GeneratorDrivenPropertyChecks

class CoercibleTest extends FlatSpec with GeneratorDrivenPropertyChecks {

  behavior of "Coercible"

  it should "coerce Left values" in {
    val left: Left[String, Float] = Left("foo")

    // Explicit type argument
    left.coerce[Left[String, Any]]
    left.coerce[Left[String, Nothing]]
    left.coerce[Left[String, String]]
    left.coerce[Left[String, Int]]

    // Infer for Left types
    left.coerce: Left[String, Any]
    left.coerce: Left[String, Nothing]
    left.coerce: Left[String, String]
    left.coerce: Left[String, Int]

    // Infer for Either types
    left.coerce: Either[String, Any]
    left.coerce: Either[String, Nothing]
    left.coerce: Either[String, String]
    left.coerce: Either[String, Int]

    // Fails at compile time for invalid types
    assertDoesNotCompile("left.coerce: Right[String, Float]")
    assertDoesNotCompile("left.coerce: Either[Float, String]")
    assertDoesNotCompile("left.coerce: Left[Float, String]")
  }

  it should "coerce Right values" in {
    val right: Right[String, Float] = Right(1.2f)

    // Explicit type argument
    right.coerce[Right[Any, Float]]
    right.coerce[Right[Nothing, Float]]
    right.coerce[Right[String, Float]]
    right.coerce[Right[Int, Float]]

    // Infer for Right types
    right.coerce: Right[Any, Float]
    right.coerce: Right[Nothing, Float]
    right.coerce: Right[String, Float]
    right.coerce: Right[Int, Float]

    // Infer for Either types
    right.coerce: Either[Any, Float]
    right.coerce: Either[Nothing, Float]
    right.coerce: Either[String, Float]
    right.coerce: Either[Int, Float]

    // Fails at compile time for invalid types
    assertCompiles("right.coerce: Right[Any, Float]")
    assertDoesNotCompile("right.coerce: Left[String, Float]")
    assertDoesNotCompile("right.coerce: Either[Float, String]")
    assertDoesNotCompile("right.coerce: Right[Float, String]")
  }

  it should "coerce F[Either[Nothing, ?]] values" in {

    import scalaz.Functor
    import scalaz.syntax.functor._

    val cachedEitherFunctor: Functor[Either[Nothing, ?]] = new Functor[Either[Nothing, ?]] {
      override def map[A, B](fa: Either[Nothing, A])(f: A => B): Either[Nothing, B] = fa.right.map(f)
    }

    implicit def eitherFunctor[L]: Functor[Either[L, ?]] = cachedEitherFunctor.coerce[Functor[Either[L, ?]]]

    assert((Right(1.2f): Either[String, Float]).void == Right(()))
    assert((Left("foo"): Either[String, Float]).void == Left("foo"))
  }

  it should "coerce TupleBuilder[Nothing]" in {

    import CoercibleTest.TupleBuilder

    assert(TupleBuilder[Int](1, 2) == (1, 2))
    assert(TupleBuilder[Float](1.2f, 2) == (1.2f, 2))
  }

  it should "not coerce Functor[Option]" in {

    import scalaz.Functor
    import scalaz.std.option._

    val F = Functor[Option]
    assertCompiles("F.map(Option(1))(_ + 1)")
    assertCompileErrorMessage("F.coerce[Functor[List]]", "Cannot coerce non-final class")
    assertCompiles("F.coerce[Functor[List]]")
  }

  private def assertCompileErrorMessage(code: String, expectedError: String): Unit = {
    try {
      assertCompiles(code)
      throw new TestFailedException(s"No compiler error, expected: $expectedError", 2)
    } catch {
      case e: TestFailedException =>
        // Will throw if e.message returns None
        e.message.get match {
          case CompilerErrorRegex(actualError) =>
            if (actualError != expectedError) {
              throw new TestFailedException(s"""Expected compiler error "$expectedError", got: $actualError""", 2)
            }
        }
    }
  }

  private val CompilerErrorRegex =
    """Expected no compiler error, but got the following [^ ]+ error: "(.*)", for code: .*""".r
}

object CoercibleTest {

  final class TupleBuilder[A] private {
    def apply[B](a: A, b: B): (A, B) = (a, b)
  }

  object TupleBuilder {
    def apply[A]: TupleBuilder[A] = instance.coerce[TupleBuilder[A]]
    private val instance = new TupleBuilder[Nothing]
  }
}
