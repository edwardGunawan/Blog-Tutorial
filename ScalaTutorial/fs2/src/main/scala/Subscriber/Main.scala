package Subscriber

import cats.effect.{ExitCode, IO, IOApp}
import fs2.Pipe
import cats.implicits._

object Main extends IOApp {

  val mockUpstream = fs2.Stream.iterate(0)(_ + 1).take(1000).covary[IO]

  def printStream[A]: Pipe[IO, A, A] =
    _.evalTap(
      a => IO(println(s"[${Thread.currentThread.getName}] downstream prints $a"))
    )

  def subscriberExample: IO[Unit] =
    for {
      subs <- Subscriber.create[IO, Int](maxBufferSize = 10, mockUpstream)
      _ <- subs.pollRepeat.through(printStream[Int]).compile.drain
    } yield ()

  override def run(args: List[String]): IO[ExitCode] = subscriberExample.as(ExitCode.Success)
}
