package GeneralizedFreeStructure

import GeneralizedFreeStructure.Dsl.Create
import GeneralizedFreeStructure.Free.{FlatMap, Pure}
import cats.{Id, Monad}

sealed trait Free[F[_], A] {

  def flatMap[B](f: A => Free[F, B]): Free[F, B] = this match {
    case Free.FlatMap(fa, fn) => FlatMap(fa, fn andThen (_ flatMap f))
    case Free.Pure(a) => f(a)
  }
  def map[B](f: A => B): Free[F, B] = flatMap(a => Pure(f(a)))
}

object Free {
  case class FlatMap[F[_], A, B](fa: F[A], f: A => Free[F, B]) extends Free[F, B]
  case class Pure[F[_], A](a: A) extends Free[F, A]
}

trait Executor[F[_], G[_]] {
  def execute[A](fa: F[A]): G[A]
}

object Dsl {
  case class Todo(id: Long, description: String, isFinished: Boolean)

  sealed trait Action[T] extends Product with Serializable
  case class Create(description: String) extends Action[Todo]
  case class Find(description: String) extends Action[Option[Todo]]
  case class Mark(id: Long) extends Action[Unit]
  case class Delete(id: Long) extends Action[Option[Todo]]
  case object Read extends Action[List[Todo]]

}

object Main extends App {
  import Dsl._
  implicit def lift[F[_], A](fa: F[A]): Free[F, A] = FlatMap(fa, Pure.apply)

  type TodoAction[A] = Free[Action, A]

  // Note that the returns `A` type is the same as the return statement of the sealed trait :
  // for instance, case class Create(description: String) extends Action[Todo] meaning it returns a
  // TodoAction[Todo]
  def create(description: String): TodoAction[Todo] = lift(Create(description))
  def find(description: String): TodoAction[Option[Todo]] = lift(Find(description))
  def mark(id: Long): TodoAction[Unit] = lift(Mark(id))
  def delete(id: Long): TodoAction[Option[Todo]] = lift(Delete(id))
  def read: TodoAction[List[Todo]] = lift(Read)

  val program =
    for {
      todo <- create("Do Laundry")
      _ <- mark(todo.id)
      listOfTodo <- read
    } yield {
      println(listOfTodo)
    }
  var map = scala.collection.mutable.Map[Long, Todo]()
  var id = 0L

  def executor = new Executor[Action, Id] {
    override def execute[T](fa: Action[T]): Id[T] = {
      fa match {
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
  }

  def runProgram[F[_], A, G[_]](program: Free[F, A], executor: Executor[F, G])(implicit M: Monad[G]): G[A] =
    program match {
      case Pure(a) => M.pure(a)
      case FlatMap(fa: F[A], f: (A => Free[F, A])) =>
        val res = executor.execute(fa)
        M.flatMap(res)(a => runProgram(f(a), executor))
    }

  runProgram(program, executor)

}
