import cats.data.State
import cats.implicits._

/**
* Functional Immutable queue. This is the regular immutable queue class on how Scala immutable queue library is constructed
 * @param vector state to contain the queue
 * @tparam A type parameter A
 */
class FunctionalQueue[+A](vector:Vector[A]) {
  // if the value that is super type of A is passed in (B), then return will be that super type B
  def enqueue[B >: A](elmt:B): FunctionalQueue[B] = new FunctionalQueue(vector :+ elmt)
  def dequeue: (A, FunctionalQueue[A]) = (vector.head, new FunctionalQueue[A](vector.tail))
  def front:A = vector.head
}


object FunctionalQueue {
  def apply[A]():FunctionalQueue[A] = new FunctionalQueue[A](Vector.empty[A])
}

