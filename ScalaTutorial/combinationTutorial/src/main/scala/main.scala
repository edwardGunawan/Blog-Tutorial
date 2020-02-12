object main extends App {
  // generating all subset, powerset
  def generateSubSet[A](lst:List[A]): List[List[A]] = {
    (lst) match {
      case Nil => List(Nil)
      case (head :: tail)=> generateSubSet(tail).map(head :: _) ::: generateSubSet(tail)
    }
  }

  val lst = generateSubSet(List(1,2,3))
  lst.foreach(println)

}
