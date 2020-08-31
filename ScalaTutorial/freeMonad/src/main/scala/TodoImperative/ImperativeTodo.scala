package TodoImperative

case class Todo(id: Long, description: String, isFinished: Boolean)

object Dsl {
  sealed trait Action[T] extends Product with Serializable
  case class Create(description: String) extends Action[Unit]
  case class Find(description: String) extends Action[Unit]
  case class Mark(id: Long) extends Action[Unit]
  case class Delete(id: Long) extends Action[Unit]
  case object Read extends Action[Unit]

}

object Interpreter {
  import Dsl._
  var map = scala.collection.mutable.Map[Long, Todo]()
  var id = 0L

  def execute[T](action: Action[T]): T = {
    action match {
      case Read =>
        println(map.values.toList)
        ().asInstanceOf[T]
      case Find(description) =>
        println(map.values.find(t => t.description == description))
        map.values.find(t => t.description == description).asInstanceOf[T]
      case Mark(id) =>
        println(
          map
            .get(id)
            .flatMap(t => map.put(id, t.copy(isFinished = !t.isFinished)))
        )
        map
          .get(id)
          .flatMap(t => map.put(id, t.copy(isFinished = !t.isFinished)))
          .asInstanceOf[T]
      case Delete(id) =>
        println(s"removing ${map.get(id)}")
        map.remove(id).asInstanceOf[T]
      case Create(description) =>
        println(s"creating todo list ${description} in id ${id}")
        (map += (id -> Todo(id, description, false)))
        id += 1
        ().asInstanceOf[T]
    }
  }

}

object ImperativeTodo extends App {
  import Dsl._

  val program = List(
    Create("Do Laundry"),
    Mark(0L),
    Read
  )

  program.foreach(t => Interpreter.execute(t))
}
