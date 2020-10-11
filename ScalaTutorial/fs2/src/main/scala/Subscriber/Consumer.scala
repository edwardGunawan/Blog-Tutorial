package Subscriber

import cats.effect.{Effect, Resource}
import cats.implicits._
import fs2.{Pipe, Stream}
import fs2.concurrent.NoneTerminatedQueue

trait Consumer[F[_], A] {
  def subscribe: Pipe[F, A, Unit]
  def subscribeNewVersion(upstream: Stream[F, A]): F[Unit]
}

object Consumer {

  def create[F[_], A](
    q: NoneTerminatedQueue[F, A]
  )(implicit F: Effect[F]): F[Consumer[F, A]] =
    F.delay(new Consumer[F, A] {

      /*
       this older version of fs2 will work because it acquire all the source and release them all at once. However,
       newer version operate as acquire and release at the same time, thus, it hangs.
       */
      override def subscribe: Pipe[F, A, Unit] = _.flatMap { i =>
        Stream.bracket(q.enqueue1(Some(i)))(_ => q.enqueue1(None))
      }

      /*
        Making a resource to stop the queue when the upstream is stop, so the downstream will stop polling
        Another way of doing this with a regular queue is using a unNoneTerminate in the downstream.
       */
      def resource(upStream: Stream[F, A]): Resource[F, Unit] =
        Resource.make {
          (for {
            a <- upStream
            _ <- Stream.eval(
              F.delay(
                println(s"[${Thread.currentThread().getName}] enqueue ${a}")
              )
            )
            _ <- Stream.eval(q.enqueue1(Some(a)))
          } yield ()).compile.drain
        }(
          release => F.delay(println(s"release - ${release}")) *> q.enqueue1(None)
        )

      override def subscribeNewVersion(upstream: Stream[F, A]): F[Unit] =
        Stream.resource(resource(upstream)).compile.drain

    })
}
