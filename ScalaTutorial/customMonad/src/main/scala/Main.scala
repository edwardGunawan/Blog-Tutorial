import cats.Monad
import scala.annotation.tailrec

import cats.implicits._


object Main extends App {
  
  val endResult = for {
    a <- CustomMonad(1)
    b <- CustomMonad(2)
  } yield {
    a + b
  }
  println(endResult)




}
