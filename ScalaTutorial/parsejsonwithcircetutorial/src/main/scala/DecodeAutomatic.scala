import io.circe.parser
import io.circe.generic.auto._

/**
  * With Auto you need to be exact in what kind of input field that you want. And when you do decoder, you need to have
  * import the io.circe.auto._ or else the compiler cannot find the implicits
  *
  * If there is none in the type you need to put in the optional so that circe doesn't think the email is required field
  *
  */
case class Form(firstName: String, lastName: String, age: Int, email: Option[String])

object Form {

  def main(args: Array[String]): Unit = {
    val inputString =
      """
        |[
        |    {"firstName": "Rose", "lastName":"Jane", "age":20, "email":"roseJane@gmail.com"},
        |    {"firstName": "John", "lastName":"Doe" , "age": 45}
        |]
        |""".stripMargin

    parser.decode[List[Form]](inputString) match {
      case Right(form) => println(form)
      case Left(ex) => println(s"Ooops something happened ${ex}")
    }
  }
}
