package WriteToFile

import cats.effect.{Blocker, Concurrent, ExitCode, IO, IOApp}
import fs2.concurrent.Queue
import cats.implicits._

object Main extends IOApp {
  val mockUpstream = fs2.Stream.iterate(0)(_ + 1).take(1000).covary[IO]

  def writeToFileExample: IO[Unit] =
    (for {
      blocker <- fs2.Stream.resource(Blocker[IO])
      q <- fs2.Stream.eval(
        Queue.bounded[IO, Option[Either[Throwable, String]]](10)
      )
      writeToFileInstance <- fs2.Stream.eval(
        WriteToFile.create[IO](q, destinationFile = "example.txt")(
          blocker,
          Concurrent[IO],
          contextShift
        )
      )
      _ <- mockUpstream
      //        .through(printStream[Int])
        .evalMap(
          i =>
            writeToFileInstance.write(
              s"[${Thread.currentThread.getName}] Coming from upstream $i"
            )
        )
    } yield ()).compile.drain

  override def run(args: List[String]): IO[ExitCode] = writeToFileExample.as(ExitCode.Success)
}
