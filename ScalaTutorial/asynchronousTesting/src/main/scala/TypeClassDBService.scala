import cats.{Applicative, Id, Traverse}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import cats.implicits._

/**
*  This method is to use type class pattern to create the get URLS and also DB instance to abstract the get method in DB client
 *  and inject it later when it is used. We only need to create
 *
 */

/*
  Type class itself
 */
trait DBClient[F[_]] {
  def get(url:String): F[Int]
}

/*
    DB Client Instances
    This can be mock depending on what kind of type ctor you want. If it is in unit test, it can be an Id. If it is
    real time, it can be a Future, or even an IO.
 */
object DBClientInstances {
  implicit val getFutureInstance: DBClient[Future] = new DBClient[Future] {
    override def get(url: String): Future[Int] = ???
  }

  implicit val getIdInstance:DBClient[Id] = new DBClient[Id] {
    override def get(url: String): Id[Int] = ???
  }
}

/*
    Interface Syntax (This is where you will restrict on what kind of type ctor you want, because it has the sumAllPrice)

    Calling in Main:
    import DBServiceSyntax._
    val dbClient = new DBClient[Future] { ....
    val urls = List(...)
    dbClient.sumAllPrice(urls)

 */
object DBServiceSyntax {
  implicit class DBServiceOps[F[_]:Applicative](dbClient:DBClient[F]) {
    def sumAllPrice(urls:List[String]): F[Int] = urls.traverse(dbClient.get).map(_.sum)
  }
}

/*
  Interface Objects (this is where you restrict on what kind of types you want because it needs to abide to sumAllPrice function)

  Calling in Main:
  import DBClientInstances._
  val urls = List(....)
  DBService.sumAllPrice(urls)
 */

object DBService {
  def sumAllPrice[F[_]:Applicative](urls:List[String])(implicit dbClient:DBClient[F]): F[Int] = urls.traverse(dbClient.get).map(_.sum)
}