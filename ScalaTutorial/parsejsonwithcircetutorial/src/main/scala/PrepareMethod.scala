import io.circe.{ACursor, Decoder, Json, JsonObject, parser}
import io.circe.generic.semiauto._
/*
  Handling Class with Default on Non Optional Field
  Using Prepare: makes you create a Acursor and with a focus which is the root of the json string that you trying to
  decode, and modify the JSON by checking if the certain field exist and needed to be alter to match the default
  attribute of the objects that you are creating. Prepare makes you modify the JSON before it circe decodes it so that
  it doesn't throw any error.

  If you just created a case class with a default argument in it. It works if the caller didn't provide that argument.
  However, if you try to decode that value with circe, it will throw decoder failure, because circe is not able to
  decode the non-optional field if you don't put optional on that field.

  Let's say on this example, not all the company provided data will have a public flag in it. Therefore, we need to
  do some work to set the public flag to false if it is not provided. We can use `Prepare` to help us modify the JSON
  before it goes to the decode value.

  Use prepare to create JSON field that aren't optional in your schema, but are optional in the defaults defined class level.

 */

case class Company(industry: String, year: Int, name: String, public: Boolean)

object Job {
  implicit val decoder: Decoder[Company] = deriveDecoder[Company].prepare { (aCursor: ACursor) =>
    {
      aCursor.withFocus(json => { // treating the incoming string json as a json object and modify the string first
        json.mapObject(jsonObject => {
          if (jsonObject.contains("public")) {
            jsonObject
          } else {
            jsonObject.add("public", Json.fromBoolean(false))
          }
        })
      })
    }
  }

  def main(args: Array[String]): Unit = {
    val inputString =
      """
        |[
        | {"industry":"tech", "year":1990, "name":"Intel", "public": true},
        | {"industry":"tech", "year":2006, "name":"Netflix"},
        | {"industry":"Consumer Goods", "year":1860, "name":"Pepsoden", "public": true}
        |]
        |""".stripMargin

    parser.decode[List[Company]](inputString) match {
      case Right(companies) => companies.map(println)
      case Left(ex) => println(s"ooops something wrong ${ex}")
    }
  }
}
