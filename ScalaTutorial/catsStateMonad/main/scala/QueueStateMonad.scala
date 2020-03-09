import StateMonad.QueueFunc
import cats.data.State
import cats.syntax.applicative._ // for pure

import cats.implicits._

/**
* Queue State Monad class has the same interface as the regular scala immutable queue, but with State monad as its implementation.
 * The caller essentially will not know if this is immutable queue that uses State monad, because all the interface of this
 * class is the same as the FunctionalQueue. The regualr immutable queue.
 * @param state
 * @tparam A
 */
class QueueStateMonad[+A](state:Vector[A]) {

  def enqueue[B >: A](elmt:B): QueueStateMonad[B] =
    new QueueStateMonad(StateMonad.enqueue(elmt).runS(state).value)

  def dequeue[B >: A](): (Option[B], QueueStateMonad[B]) = {
    val (newQueue, topOption) = StateMonad.dequeue.run(state).value

    (topOption, new QueueStateMonad(newQueue))
  }

  def front[B >: A]() :Option[B] = state.headOption
}

object QueueStateMonad {
  def apply[A](): QueueStateMonad[A] = new QueueStateMonad(Vector.empty[A])

  def apply[A](vector:Vector[A]): QueueStateMonad[A] = {
    // usually the runS or the initial provided value will be an empty and the enqueueAll will evaluate
    // all the state monad
    // it doesn't really matter what kind of input is provided in runS because it won't be use in StateMonad.enqueueAll
    // you will use it if you provide a State ctor
    val queueState = StateMonad.enqueueAll(vector).runS(Vector.empty[A]).value
    new QueueStateMonad(queueState)
  }



}

/**
*   State monad uses State monad [S,A] where S represent the state, and A represent the result.
 *   This is essentially the underlying data structure logic of  QueueStateMonad.
 *
 *   Caller can use this StateMonad object (QueueFunc) to construct immutable queue and do operation without
 *   explicitly passing around its state objects. Reduce the error-prone boilerplate.
 */
object StateMonad {
  type QueueFunc[A] = State[Vector[A],Option[A]]

  def enqueue[A](elmt:A): QueueFunc[A] = State[Vector[A], Option[A]]{ oldVector =>
    (oldVector :+ elmt, oldVector.headOption)
  }

  def dequeue[A]: QueueFunc[A] = State[Vector[A],Option[A]] { oldVector =>
    (oldVector.tail, oldVector.headOption)
  }

  def enqueueAll[A](vector:Vector[A]): QueueFunc[A] = vector.foldLeft(StateMonad.init[A]){(a,b) =>
    for{
      _ <- a
      res <- StateMonad.enqueue(b)
    } yield res
  }

  def init[A]:QueueFunc[A] = State[Vector[A], Option[A]] {
    case v if v.isEmpty => (v, None)
    case oldVector => (oldVector,oldVector.head.some)
  }
}
