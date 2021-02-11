package circuitBreaker

import java.util.concurrent.TimeUnit

import cats.effect.{Clock, ContextShift, IO, Timer}
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.must.Matchers
import retry._
import retry.CatsEffect._
import cats.implicits._
import scala.concurrent.duration._
import retry.RetryDetails._

import scala.concurrent.{ExecutionContext, Future}

class CircuitBreakerSpec extends AsyncFreeSpec with Matchers {
  implicit val timer: Timer[IO] = IO.timer(ExecutionContext.global)
  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  val callSuccess: IO[String] = IO("Success Call")
  val callFailure: IO[String] = IO.raiseError(new Exception("Failure call"))

  def logError(err: Throwable, details: RetryDetails): IO[Unit] = details match {
    case RetryDetails.WillDelayAndRetry(nextDelay, retriesSoFar, cumulativeDelay) =>
      IO(
        println(s"Failure. So far we have retried ${retriesSoFar} times.")
      )
    case RetryDetails.GivingUp(totalRetries, totalDelay) =>
      IO(println(s"Giving up after total retry ${totalRetries}, error ${err.getMessage}"))
  }

  val breakerConfig = CircuitBreakerConfig(maxNumberRetry = 3, offsetTimeout = 30)

  "CircuitBreaker" - {
    "Calling Successful request on breaker close should return successful" in {

      val program = CircuitBreaker.create[IO](breakerConfig).flatMap { circuitBreaker =>
        circuitBreaker.protect(callSuccess)
      }

      program.unsafeRunSync must equal(callSuccess.unsafeRunSync)
    }

    "Calling Successful on breaker open should default to failure" in {

      val program =
        for {
          now <- Clock[IO].realTime(TimeUnit.SECONDS)
          cb <- CircuitBreaker.create[IO](breakerConfig, Open(now + breakerConfig.offsetTimeout, None))
          _ <- cb.protect(callSuccess)
        } yield ()

      assertThrows[CircuitBreakerException](program.unsafeRunSync)
    }

    "Calling Failure on breaker close multiple times turn breaker to open" in {
      val circuitBreaker = CircuitBreaker.create[IO](breakerConfig).unsafeRunSync()
      val program = for {
        _ <- retryingOnAllErrors[String](policy = RetryPolicies.limitRetries[IO](4), onError = logError)(
          circuitBreaker.protect(callFailure)
        )
      } yield ()

      program.attempt.unsafeRunSync().left.get mustBe a[CircuitBreakerException]
      circuitBreaker.getStatus.unsafeRunSync mustBe a[Open]
    }

    "Calling failure on breaker one time and then success on the second ones on close status will turn breaker to open" in {
      val offsetTimeout = 10L
      val retryPolicies = RetryPolicies.limitRetries[IO](2) |+| RetryPolicies.constantDelay[IO]((offsetTimeout).seconds)

      val circuitBreaker = for {
        now <- Clock[IO].realTime(TimeUnit.SECONDS)
        circuitBreaker <- CircuitBreaker
          .create[IO](breakerConfig.copy(offsetTimeout = offsetTimeout), Open(now + 5, Some("Open Breaker Test")))
        str <- retryingOnAllErrors[String](policy = retryPolicies, onError = logError)(
          circuitBreaker.protect(callSuccess)
        )
      } yield (str)

      circuitBreaker.unsafeRunSync() must equal(callSuccess.unsafeRunSync())

    }

    "Calling multiple call on for a long period of time will turn the breaker to half open" in {
      val offsetTimeout = 10L
      val retryThreeTimes5s = RetryPolicies.limitRetries[IO](3) |+| RetryPolicies.constantDelay[IO](
        (offsetTimeout - 5).seconds
      )
      val retryThreeTimes10s = RetryPolicies.limitRetries[IO](3) |+| RetryPolicies.constantDelay[IO](
        (offsetTimeout + 1).seconds
      )
      val circuitBreaker = for {
        now <- Clock[IO].realTime(TimeUnit.SECONDS)
        circuitBreaker <- CircuitBreaker
          .create[IO](breakerConfig)
        // 3 failures will open the breaker
        fail = retryingOnAllErrors[String](policy = retryThreeTimes5s, onError = logError)(
          circuitBreaker.protect(callFailure)
        )
        // calling the first 2 will fail and the third one will go to half open and will succeed
        suc = retryingOnAllErrors[String](policy = retryThreeTimes10s, onError = logError)(
          circuitBreaker.protect(callSuccess)
        )
        str <- fail.handleErrorWith(_ => IO(println("Executing success")) >> suc)
      } yield (str)

      circuitBreaker.unsafeRunSync() must equal(callSuccess.unsafeRunSync())
    }

    "Calling multiple concurrent failed service on a one circuit breaker will set the breaker to open" in {
      val program =
        CircuitBreaker.create[IO](breakerConfig).flatMap { circuitBreaker =>
          val protetedCb = circuitBreaker.protect(callFailure)
          val p = for {
            r1 <- protetedCb.start
            r2 <- protetedCb.start
            r3 <- protetedCb.start
            r4 <- protetedCb.start
            _ <- r1.join
            _ <- r2.join
            _ <- r3.join
            _ <- r4.join
          } yield ()
          p.handleErrorWith(_ => circuitBreaker.getStatus)
        }

      program.unsafeRunSync() mustBe a[Open]
    }
  }

}
