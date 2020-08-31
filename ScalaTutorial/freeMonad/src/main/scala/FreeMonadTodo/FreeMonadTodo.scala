package FreeMonadTodo

sealed trait Free[F[_], A] {
  import Free._

  def flatMap[B](fn: A => Free[F, B]): Free[F, B] = this match {
    case Free.Pure(a) =>
      fn(a)
    case Free.FlatMap(fa, fun) =>
      FlatMap(fa, fun andThen (a => a.flatMap(fn)))
  }
  def map[B](fn: A => B): Free[F, B] = flatMap(a => Pure(fn(a)))
}

object Free {
  final case class FlatMap[A, B, F[_]](fa: F[A], fun: A => Free[F, B]) extends Free[F, B]
  final case class Pure[F[_], A](a: A) extends Free[F, A]

}

object FreeMonadTodo extends App {
  import Free._
  implicit def lift[A](fa: Action[A]): Free[Action, A] = FlatMap(fa, Pure.apply)

  case class Todo(id: Long, description: String, isFinished: Boolean)

  sealed trait Action[T] extends Product with Serializable
  case class Create(description: String) extends Action[Todo]
  case class Find(description: String) extends Action[Option[Todo]]
  case class Mark(id: Long) extends Action[Unit]
  case class Delete(id: Long) extends Action[Option[Todo]]
  case object Read extends Action[List[Todo]]

  val program =
    for {
      todo <- Create("Do Laundry")
      _ <- Mark(todo.id)
      listOfTodo <- Read
    } yield {
      println(listOfTodo)
    }

  var map = scala.collection.mutable.Map[Long, Todo]()
  var id = 0L

  def execute[T](action: Action[T]): T = {
    action match {
      case Read =>
        println(map.values.toList)
        map.values.toList.asInstanceOf[T]
      case Find(description) =>
        println(map.values.find(t => t.description == description))
        map.values.find(t => t.description == description).asInstanceOf[T]
      case Mark(id) =>
        println(
          map
            .get(id)
            .flatMap(t => map.put(id, t.copy(isFinished = !t.isFinished)))
        )
        ().asInstanceOf[T]
      case Delete(id) =>
        println(s"removing ${map.get(id)}")
        map.remove(id).asInstanceOf[T]
      case Create(description) =>
        println(s"creating todo list ${description} in id ${id}")
        val todo = Todo(id, description, false)
        (map += (id -> Todo(id, description, false)))
        id += 1
        todo.asInstanceOf[T]
    }
  }

  def runProgram[A](program: Free[Action, A]): A = program match {
    case Free.Pure(a) => a
    case FlatMap(fa: Action[A], fn: (A => Free[Action, A])) =>
      // execute the Action here

      val res = execute(fa)
      // thread the function into a new Free
      val newFree = fn(res)
      // execute the next function
      runProgram(newFree)
  }

  runProgram(program)

}
