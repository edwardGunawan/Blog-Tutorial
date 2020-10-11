package Subscriber
import cats.effect.{ConcurrentEffect, ContextShift, Resource}
import cats.implicits._
import fs2.Stream
import fs2.concurrent.{NoneTerminatedQueue, Queue}

trait Subscriber[F[_], A] {
  def pollRepeat: Stream[F, A]
}

object Subscriber {

  def create[F[_], A](maxBufferSize: Int, upstream: Stream[F, A])(
    implicit F: ConcurrentEffect[F],
    cs: ContextShift[F]
  ): F[Subscriber[F, A]] = {

    val consumer: F[NoneTerminatedQueue[F, A]] =
      for {
        q <- Queue.boundedNoneTerminated[F, A](maxBufferSize)
        c <- Consumer.create[F, A](q)
        _ <- F.start(c.subscribeNewVersion(upstream)) // this process will fire off another thread
      } yield (q)

    consumer.map(
      q =>
        new Subscriber[F, A] {
          override def pollRepeat: Stream[F, A] =
            q.dequeue.evalTap(
              a =>
                F.delay(
                  println(s"[${Thread.currentThread.getName}] dequeing $a")
                )
            )
        }
    )

  }
}
