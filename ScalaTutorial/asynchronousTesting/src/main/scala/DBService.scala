import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global


trait DBClient {
  def get(url:String):Future[Int]
}

class DBService(dbClient:DBClient) {
  def sumAllPrice(urls:List[String]): Future[Int] = Future.traverse(urls)(dbClient.get).map(_.sum)
}

