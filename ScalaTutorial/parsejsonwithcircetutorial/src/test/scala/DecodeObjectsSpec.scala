import io.circe.parser
import org.scalatest.{MustMatchers, WordSpec}

class DecodeObjectsSpec extends WordSpec with MustMatchers {

  "Decode Objects" should {
    "decode product json object into a product object pojo" in {
      val inputString: String =
        """
          |{
          |   "productId": 111112222222,
          |   "price": 23.45,
          |   "countryCurrency": "USD",
          |   "inStock": true
          |}
          |""".stripMargin

      val productStub = Product(111112222222L, 23.45, "USD", true)

      parser.decode[Product](inputString) match {
        case Right(product) => product must equal(productStub)
      }
    }
  }

}
