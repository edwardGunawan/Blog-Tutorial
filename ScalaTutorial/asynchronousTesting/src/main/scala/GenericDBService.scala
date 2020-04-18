import cats.{Applicative, Id}
import cats.implicits._

import scala.concurrent.Future

/*
  Didn't force the DB Client to be a certain type. But forcing it when using it in the GenericDBService
 */
trait DBClient[F[_]] {
  def get(url:String):F[Int]
}

trait TestDBClient extends DBClient[Id]

/*
  Over here we enforce the type ctor to be more strict to applicative so that it can have applicative like attribute
 */
class GenericDBService[F[_]:Applicative](dbClient: DBClient[F]) {
  def sumAllPrice(urls:List[String]): F[Int] = urls.traverse(dbClient.get).map(_.sum)
}
