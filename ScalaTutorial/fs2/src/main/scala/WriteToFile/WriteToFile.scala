package WriteToFile

import java.nio.file.Paths

import cats.effect.{Blocker, Concurrent, ContextShift}
import fs2.concurrent.Queue
import fs2.{Pipe, io, text}
import cats.implicits._

trait WriteToFile[F[_]] {
  def write(item: String): F[Unit]

}

object WriteToFile {

  def create[F[_]](
    queue: Queue[F, Option[Either[Throwable, String]]],
    destinationFile: String
  )(implicit blocker: Blocker, F: Concurrent[F], cs: ContextShift[F]): F[WriteToFile[F]] = {

    def toFile(fileName: String): Pipe[F, String, Unit] =
      _.intersperse("\n")
        .evalTap(
          s =>
            F.delay(
              println(
                s"[${Thread.currentThread().getName}] Writing $s to $fileName"
              )
            )
        )
        .through(text.utf8Encode)
        .through(io.file.writeAll(Paths.get(fileName), blocker))

    def constantPoll: fs2.Stream[F, Unit] =
      fs2.Stream.bracket(
        queue.dequeue
          .evalTap(i => F.delay(println(s"print ${i}")))
          .unNoneTerminate
          .rethrow
          .through(toFile(destinationFile))
          .evalTap(
            str =>
              F.delay(
                println(s"[${Thread.currentThread().getName}] dequeuing - $str")
              )
          )
          .compile
          .drain
      )(_ => queue.enqueue1(None))

    // initialized the dequeue Stream here
    for {
      _ <- F.start(constantPoll.compile.drain)
      writeToFileInstance <- F.delay {
        new WriteToFile[F] {
          override def write(item: String): F[Unit] =
            F.delay(
              println(s"[${Thread.currentThread.getName} enqueing - $item ]")
            ) *> queue.enqueue1(Some(Right(item)))
        }
      }
    } yield (writeToFileInstance)

  }

}
