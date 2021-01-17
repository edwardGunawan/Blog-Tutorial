import scala.annotation.tailrec
import scala.util.control.TailCalls.TailRec

sealed trait Trampolining[-A] {
  def flatMap[B, C <: A](f: C => Trampolining[B]): Trampolining[B] = FlatMap(this, f)
  def map[B, C <: A](f: C => B): Trampolining[B] = flatMap(f andThen (Return(_))) // or flatMap(a => Return(f(a)))
}

case class Return[A](a: A) extends Trampolining[A]
case class Suspense[A](a: () => Trampolining[A]) extends Trampolining[A]
case class FlatMap[A, B](a: Trampolining[A], f: A => Trampolining[B]) extends Trampolining[B]

object Trampolining extends App {

  /*
    Assumed that we want to create a fib function and this is not tail recursive
   */
  def fib(n: Int): Int = {
    if (n == 0) 0
    else if (n == 1) 1
    else fib(n - 1) + fib(n - 2)
  }

//  fib(100000)

  /*
    Fib with TailRec
   */
  def fibTailRef(n: Int): Trampolining[Int] = {
    if (n == 0) Return[Int](0)
    else if (n == 1) Return[Int](1)
    else {
      FlatMap[Int, Int](
        Suspense(() => fibTailRef(n - 1)),
        i => FlatMap[Int, Int](Suspense(() => fibTailRef(n - 2)), j => Return[Int](i + j))
      )
      Suspense(() => fibTailRef(n - 1)).flatMap[Int, Int](i => {
        Suspense(() => fibTailRef(n - 2)).map[Int, Int](_ + i)
      })
//      FlatMap[Int, Int](fibTailRef(n - 1), i => FlatMap[Int, Int](fibTailRef(n - 2), j => Suspense[Int](() => (i + j))))
//      fibTailRef(n - 1).flatMap[Int, Int](i => Suspense(() => fibTailRef(n - 2)).map[Int, Int](_ + i))
    }
//      FlatMap[Int, Int](fibTailRef(n - 1), i => FlatMap[Int, Int](fibTailRef(n - 2), j => Suspense(() => i + j)))
  }

  /*
    having an Interpreter to interpret the results in a tailrec manner
   */
  @tailrec
  def run(trampoline: Trampolining[Int]): Int = trampoline match {
    case Return(a: Int) => a
    case Suspense(x: (() => Trampolining[Int])) =>
      run(x())

    case FlatMap(x: Trampolining[Int], f: (Int => Trampolining[Int])) =>
      x match {
        case Return(a: Int) => run(f(a))
        // it lies in here that `run(a())` needs to be run(FlatMap(a(), f))
        case Suspense(a: (() => Trampolining[Int])) => run(FlatMap(a(), f))
        case FlatMap(x1: Trampolining[Int], f1: (Int => Trampolining[Int])) =>
          // x1 flatMap(f1 andThen (_ flatMap f)) // is not tail recurse because of andThen
          run(x1.flatMap((x2: Int) => f1(x2).flatMap(f): Trampolining[Int]))
      }
  }

  println("Print here" + run(fibTailRef(10)))

}
