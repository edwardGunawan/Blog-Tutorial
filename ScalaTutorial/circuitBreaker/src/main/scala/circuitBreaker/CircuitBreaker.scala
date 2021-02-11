package circuitBreaker

import java.time.Instant

import cats.{Applicative, MonadError}
import cats.effect.{Clock, Concurrent, ExitCode, IO, IOApp, Sync}
import cats.effect.concurrent._

import scala.concurrent.duration._
import cats.implicits._

import scala.concurrent.ExecutionContext

/*
    Creating Circuit breaker that returns either a successful or a failure - did a retry multiple times,
    and if over a certain number of time it failed it will set the state to Closed State.
    After a certain amount of time it will go from Closed to Half Open
    We can then try doing some calls, and if it is successful it will set it to open again.
    3 state (Open, HalfOpen, Closed)
 */
sealed trait Status
case class Open(openTimestamp: Long, errMsg: Option[String]) extends Status
case class Close(errCount: Int) extends Status

case class CircuitBreakerException(errMsg: String) extends Exception

case class CircuitBreakerConfig(maxNumberRetry: Int, offsetTimeout: Long)

trait CircuitBreaker[F[_]] {
  def protect[A](f: => F[A]): F[A]
  def getStatus: F[Status]
}

object Util {
  def getCurrentTime[F[_]](implicit clock: Clock[F]): F[Long] = clock.realTime(SECONDS)

  def toOption(msg: String): Option[String] = if (msg.trim.isEmpty) None else msg.some
}

object CircuitBreaker {

  def create[F[_]](
    config: CircuitBreakerConfig
  )(implicit clock: Clock[F], concurrent: Concurrent[F]): F[CircuitBreaker[F]] = createHelper[F](config, Close(0))

  def create[F[_]](config: CircuitBreakerConfig, initialStatus: Status)(
    implicit clock: Clock[F],
    concurrent: Concurrent[F]
  ): F[CircuitBreaker[F]] = createHelper[F](config, initialStatus)

  private[CircuitBreaker] def createHelper[F[_]](config: CircuitBreakerConfig, status: Status)(
    implicit clock: Clock[F],
    concurrent: Concurrent[F]
  ): F[circuitBreaker.CircuitBreaker[F]] = Ref.of[F, Status](status).map { state =>
    new CircuitBreaker[F] {
      override def protect[A](f: => F[A]): F[A] = getStatus.flatMap {
        case Open(_, _) => executeOpenBreaker(f)
        case Close(_) => executeCloseBreaker(f)
      }

      def executeOpenBreaker[A](f: => F[A]): F[A] =
        for {
          now <- Util.getCurrentTime[F]
          maybeErrMsg <- state.modify[Option[String]] {
            case o @ Open(openTimestamp, errMsg) =>
              if (now > openTimestamp) {
                (o, None)
              } else (o, Some(errMsg.getOrElse("No Error Message")))
            case c @ Close(_) => (c, None)
          }
//          _ <- concurrent.delay(println(s"${if (maybeErrMsg.isEmpty) "execute half open" else "execute error call"}"))
          ret <- if (maybeErrMsg.isEmpty) executeHalfOpenBreaker(f) else errorCall[A](maybeErrMsg)
        } yield (ret)

      def executeCloseBreaker[A](f: => F[A]): F[A] =
        f handleErrorWith {
          case throwable: Throwable =>
            for {
              now <- Util.getCurrentTime[F]
              maybeMessage <- concurrent.delay(Util.toOption(throwable.getMessage))
              // set the state to Open and input the error message
              status <- state.modify {
                case Open(openTimestamp, errMsg) => {
                  val o = Open(openTimestamp + config.offsetTimeout, maybeMessage)
                  (o, o)
                }
                case Close(errCount) =>
                  if (errCount >= config.maxNumberRetry) {
                    val o = Open(openTimestamp = now + config.offsetTimeout, errMsg = maybeMessage)
                    (o, o)
                  } else {
                    val c = Close(errCount + 1)
                    (c, c)
                  }
              }
//              _ <- concurrent.delay(println(s"Status after executing the error call ${status}"))
              err <- errorCall[A](maybeMessage) // raise the error
            } yield (err)
        }

      def executeHalfOpenBreaker[A](f: => F[A]): F[A] =
        for {
          a <- executeCloseBreaker(f)
//          _ <- concurrent.delay(println("Success executing halfOpen Breaker - set the Close to 0"))
          _ <- state.set(Close(0)) // if the above execution successful reset the state to Close with errorCount to 0
        } yield a

      def errorCall[A](msg: Option[String]): F[A] =
        Concurrent[F].raiseError(CircuitBreakerException(msg.getOrElse("No Error Message")))

      override def getStatus: F[Status] = state.get
    }
  }
}
