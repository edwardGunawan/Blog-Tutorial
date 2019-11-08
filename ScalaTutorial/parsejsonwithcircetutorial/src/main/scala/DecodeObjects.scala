import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto._
import io.circe.parser._

case class Product(productId: Long, price: Double, countryCurrency: String, inStock: Boolean)

object Product {
  implicit val decoder: Decoder[Product] = deriveDecoder[Product]
  implicit val encoder: Encoder[Product] = deriveEncoder[Product]

  def main(args: Array[String]): Unit = {
    val inputString: String =
      """
        |{
        |   "productId": 111112222222,
        |   "price": 23.45,
        |   "countryCurrency": "USD",
        |   "inStock": true
        |}
        |""".stripMargin

    decode[Product](inputString) match {
      case Right(productObject) => println(productObject)
      case Left(ex) => println(s"Ooops some errror here ${ex}")
    }
  }
}
