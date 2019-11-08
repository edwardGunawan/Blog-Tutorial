import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.{ActorMaterializer, Materializer}
import io.circe.Decoder.Result
import io.circe.{Decoder, Encoder, HCursor, parser}
import io.circe.generic.semiauto._
import io.circe.parser._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class Employee(id: String, employeeName: String, employeeSalary: String, employeeAge: String, profileImage: String)

object Employee {
  implicit val encoder: Encoder[Employee] = deriveEncoder[Employee]
  implicit val decoder: Decoder[Employee] = (hCursor: HCursor) =>
    for {
      id <- hCursor.get[String]("id")
      employeeName <- hCursor.get[String]("employee_name")
      employeeSalary <- hCursor.get[String]("employee_salary")
      employeeAge <- hCursor.get[String]("employee_age")
      profileImage <- hCursor.get[String]("profile_image")
    } yield Employee(id, employeeName, employeeSalary, employeeAge, profileImage)
}

trait HttpClient {
  def sendRequest(httpRequest: HttpRequest)(implicit actorSystem: ActorSystem): Future[HttpResponse]
}

// we will be mocking this
trait ClientHandler extends HttpClient {
  override def sendRequest(httpRequest: HttpRequest)(implicit actorSystem: ActorSystem): Future[HttpResponse] = {
    Http().singleRequest(httpRequest)
  }

  def shutDown()(implicit actorSystem: ActorSystem): Unit = {
    Http().shutdownAllConnectionPools()
  }
}

trait EmployeeRestClient { this: HttpClient =>
  implicit def actorSystem: ActorSystem
  implicit def executionContext: ExecutionContext
//  implicit def materializer: Materializer

  def getEmployees(url: String): Future[List[Employee]] = {
    for {
      response <- sendRequest(HttpRequest(uri = url))
      employeeString <- Unmarshal(response.entity).to[String]
      employeeObject <- parser.decode[List[Employee]](employeeString) match {
        case Right(employeeObj) => Future(employeeObj)
        case Left(err) => Future.failed(err)
      }
    } yield { employeeObject }
  }
}

object EmployeeRestClient extends App {
  implicit def actorSystem: ActorSystem = ActorSystem()
  implicit val executionContext: ExecutionContext = actorSystem.dispatcher

  val newRestClient = new EmployeeRestClient() with ClientHandler {
    override implicit def actorSystem: ActorSystem = ActorSystem()
    override implicit def executionContext: ExecutionContext = actorSystem.dispatcher
  }
  newRestClient.getEmployees("http://dummy.restapiexample.com/api/v1/employees").onComplete { res =>
    res match {
      case Success(employees) => employees.map(println)
      case Failure(exception) => println(s"Failed to fetch ... ${exception.getMessage}")
    }
    newRestClient.shutDown()
  }
}
