package example
import io.circe._
import io.circe.generic.semiauto._

case class RequestFormatter(helloWorld: String, number: Int, isRequested: Boolean)

object RequestFormatter {
  implicit val encode: Encoder[RequestFormatter] = deriveEncoder[RequestFormatter]
  implicit val decode: Decoder[RequestFormatter] = deriveDecoder[RequestFormatter]
}
