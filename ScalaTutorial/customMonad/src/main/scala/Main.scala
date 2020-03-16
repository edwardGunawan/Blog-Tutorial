import cats.Monad
import scala.annotation.tailrec
object Main extends App {
  val optionMonad = new Monad[Option] {
    override def pure[A](x: A): Option[A] = Some(x)

    override def flatMap[A, B](fa: Option[A])(f: A => Option[B]): Option[B] = fa flatMap(f)

    @tailrec
    override def tailRecM[A, B](a: A)(f: A => Option[Either[A, B]]): Option[B] = f(a) match {
      case None => None
      case Some(Left(a)) => tailRecM(a)(f)
      case Some(Right(b)) => Some(b)
    }
  }


}
