import cats.data.State

object Main extends App {

  /*
      Regular Immutable Queue
   */
  println(s"creating immutable queue without State monad")
  val functionalQueue = FunctionalQueue[Int]
  println(s"enqueue 1 immutable queue")
  val enqueue1 = functionalQueue.enqueue(1)
  println(s"enqueue 2 immutable queue")
  val enqueue2 = enqueue1.enqueue(2)
  println(s"front ${enqueue2.front}")
  val (head, rest) = enqueue2.dequeue
  println(s"dequeue head: ${head}  rest : ${rest}")



  /*
    Immutable queue with State monad
   */

  println(s"creating immutable queue with State Monad")
  val stateMonadQueue = QueueStateMonad[Int]
  println(s"enqueue 1 immutable queue ")
  val stateEnqueue1 = stateMonadQueue.enqueue(1)
  println(s"enqueue 2 immutable queue")
  val stateEnqueue2 = stateEnqueue1.enqueue(2)
  println(s"front ${stateEnqueue2.front}")
  val (h, tl) = stateEnqueue2.dequeue()
  println(s"dequeue head :${h} rest : ${tl}")

  println("initializing with Vector ...")
  val queue = QueueStateMonad[Int](Vector(1,2,3,4,5))
  println(s"get Front ${queue.front()}")

  /*
    Using StateMonad to enqueue and dequeue
    In this example, we will do the same by enqueue 1, 2 and dequeue it
    The underlying behavior is create all of the constructions, then pass in a value to the program to execute
    all the construction.
   */
  println("Executing StateMonad")
  val program = for {
    _ <- StateMonad.init[Int]
    _ <- StateMonad.enqueue(1)
    _ <- StateMonad.enqueue(2)
    rest <- StateMonad.dequeue
  } yield {
    println(s"result that is dequeue ${rest}")
    rest
  }

  val (state, value ) = program.run(Vector.empty[Int]).value
  println(s"state - ${state.map(st => println(s"inside state - ${st}"))}  value - ${value}")


  /**
  * State example: combination using flatMap, and map
   */
  val plus1 = State[Int, String]{state =>
    (state+1, s"The result of this state is ${state+1}")
  }

  val plus2 = State[Int,String] {state =>
    (state +2, s"The result of this state is ${state+2}")
  }

  val historyProgram = for {
    historyOne <- plus1
    historyTwo <- plus2
  } yield List(historyOne, historyTwo)

  val (result, history) = historyProgram.run(0).value
  println(s"result ${result} history ${history}")




}
