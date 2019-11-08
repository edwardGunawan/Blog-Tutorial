import io.circe.Decoder.Result
import io.circe.{Decoder, HCursor, parser}
import io.circe.generic.semiauto._

/**
  * In this decoding, you need to specify what kind of data structure that you want to have for decoder.
  * For instance, create the value as one object, but when decoding it you decode it as an array List[Book]
  * Usually if it is all array, case class will serve as the representation of that one single object in the array.
  * When decoding it, don't forget to specify the implicit evidence of the representation as the List[ThatType]
  *
  */
case class Book(book: String)

object Book {
  implicit val decoder: Decoder[Book] = deriveDecoder[Book]

  def main(args: Array[String]): Unit = {
    val inputString =
      """
        |[
        | {"book": "Programming in Scala"},
        | {"book": "How to Win Friends and Influence People"},
        | {"book": "HomoSapiens"},
        | {"book": "Scala OOP"}
        |]
        |""".stripMargin

    parser.decode[List[Book]](inputString) match {
      case Right(books) => println(s"Here are the books ${books}")
      case Left(ex) => println(s"Ooops something error ${ex}")
    }
  }
}
