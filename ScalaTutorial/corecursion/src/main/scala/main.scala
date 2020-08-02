object main extends App {
  /**
  * unfold function
   */
  def unfold[A,S](s:S)(f: S => Option[(A,S)]): List[A] = f(s) match {
    case None => Nil
    case Some((currResult, nextState)) => currResult :: unfold(nextState)(f)
  }

  /**
  * Fibonnaci Series
   */
   def fib(n:Int): Int = generateFibSequence(n).last


  def generateFibSequence(n:Int): List[Int] =
    unfold((n, (0,1))){s => s match {
      case ((num, (f0, f1))) if(num > 0) => Some((f0, (num -1, (f1, f0+f1))))
      case _ => None // terminate
    }}


  /**
  * Factorial
   */
  def factorialGenerator(n:Int): List[Int] = unfold((1, 1)){
    case ((num,_)) if (num > n) =>  None
    case ((num, currFactorial)) =>
      val nextFactorialValue = currFactorial * num
      Some(( nextFactorialValue, (num+1, nextFactorialValue)))
  }

  /**
  * BFS
   */
  sealed trait Graph[+T]
  case class Node[T](value:T, child:List[Graph[T]]) extends Graph[T]


  def bfs[T](root:Graph[T]): List[T] =
    unfold(List(root)){
      case Node(value,child) :: t =>
        val newQueue = t ++ child
        Some((value, newQueue))
      case Nil => None
    }

  println(fib(3))
  println(factorialGenerator(4))

  val graph = Node(1, List(Node(2, List.empty), Node(3,List.empty), Node(4, List(Node(5, List.empty)))))
  println(bfs(graph))


}
