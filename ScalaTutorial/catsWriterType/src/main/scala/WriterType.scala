package main.scala
import cats.data._
import cats.implicits._

import scala.concurrent.{Await, ExecutionContext, Future}

// uncomment all the print statement to know what it will print out
object WriterType extends App {

  // Extract result and Log Type respectively with written and value
  val a = Writer(Vector("msg1"),0)
  val log = a.written
  val result = a.value

//  println(s"log: $log result: $result")

  // Composing and Transforming Writer
  val res = for {
    a <- Writer(Vector("a"), 1)
    _ <- Vector("c").tell
    b <- 3.writer(Vector("3", "b"))
  } yield {
//    println(s"a $a")
//    println(s"b $b")
    a + b
  }

//  println(res)

  // mapWritten
  val upperCaseLog = res.mapWritten(previousLog => previousLog.map(_.toUpperCase))
//  println(upperCaseLog)

  // mapBoth
  val newWriterValueAndLog = res.mapBoth{ (log,res) =>
    (log :+ "appending z", res+12)
  }
//  println(newWriterValueAndLog)

  // swap
  val swappedWriter = res.swap
//  println(swappedWriter)

  // reset
  val resetWriter = res.reset
//  println(resetWriter)


  // Writer in Action


  type LogFib[A] = Writer[Vector[String], A]
  def timeout[A](body: => A):A = try {
    body
  } finally Thread.sleep(100)

  // original Fibonacci
  def fibOriginal(n:Int) : Int = {
    timeout(
      if(n==0 || n == 1) {
        println(s"base case : $n")
        n
      }else {
        println(s"add fib(n-1) + fib(n-2) $n")
        fibOriginal(n-1) + fibOriginal(n-2)
      }
    )
  }

  // Writer Example Fibonacci
  def fib(n:Int): LogFib[Int] = {
    timeout(
      if(n == 0 || n ==1) {
        n.writer(Vector(s"base case : $n"))
      }
      else {
        for {
          _ <- Vector(s"add fib(n-1) + fib(n-2) $n").tell
          fib1 <- fib(n-1)
          fib2 <- fib(n-2)
        } yield fib1 + fib2
      }
    )
  }

//  println(fib(5).run)

  // running multiple fib in parallel
  import scala.concurrent.duration._
  implicit val ec :ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  val fibRes = Await.result(Future.sequence(Vector(
    Future(fib(5)),
    Future(fib(4)),
    Future(fib(3))
  ))
  , Duration.Inf)


  fibRes.toList.map(w => {
    val (logging, endResult) = w.run
    println(s"logging $logging endResult $endResult")
  })


}
