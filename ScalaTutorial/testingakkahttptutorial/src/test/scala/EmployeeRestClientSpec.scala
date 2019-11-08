import akka.actor.ActorSystem
import akka.actor.Status.Success
import akka.http.scaladsl.model.{HttpEntity, HttpMethod, HttpRequest, HttpResponse, ResponseEntity}
import akka.stream.Materializer
import akka.testkit.TestKit
import akka.util.ByteString
import io.circe.parser
import org.scalamock.scalatest.{AsyncMockFactory, MockFactory}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, MustMatchers, WordSpecLike}

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._
import scala.language.postfixOps

class EmployeeRestClientSpec
    extends TestKit(ActorSystem("EmployeeRestClientSpec"))
    with MustMatchers
    with WordSpecLike
    with ScalaFutures
    with MockFactory
    with BeforeAndAfterAll {

  trait MockClientHandler extends HttpClient {
    val mock = mockFunction[HttpRequest, Future[HttpResponse]]

    override def sendRequest(httpRequest: HttpRequest)(implicit actorSystem: ActorSystem): Future[HttpResponse] =
      mock(httpRequest)
  }

  "Employee Rest Client" should {
    "work on HttpClient" in {
      val stripString =
        """
          |[{
          |"id": "1",
          |"employee_name": "Salome",
          |"employee_salary": "457",
          |"employee_age": "35",
          |"profile_image": ""
          |},
          |{
          |"id": "34",
          |"employee_name": "ABC",
          |"employee_salary": "666",
          |"employee_age": "50",
          |"profile_image": ""
          |}]
          |""".stripMargin

      // mock Http
      val employeeRestClient = new EmployeeRestClient with MockClientHandler {
        override implicit def actorSystem: ActorSystem = system
        override implicit def executionContext: ExecutionContext = ExecutionContext.Implicits.global
      }

      employeeRestClient.mock
        .expects(HttpRequest(uri = "http://dummy.restapiexample.com/api/v1/employees"))
        .returning(Future.successful(HttpResponse(entity = HttpEntity(ByteString(stripString)))))

      val expectResult = parser.decode[List[Employee]](stripString) match {
        case Right(employees) => employees
        case Left(_) => throw new Exception
      }

      whenReady(employeeRestClient.getEmployees("http://dummy.restapiexample.com/api/v1/employees")) { res =>
        res must equal(expectResult)
      }
    }
  }

}
