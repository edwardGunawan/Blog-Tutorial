import io.circe.{Decoder, DecodingFailure, Encoder, HCursor, Json}
import io.circe.generic.semiauto._
import io.circe.syntax._
import io.circe.parser._
import cats.implicits._

/*
  Decoding a regular String to a Coproduct. However, the string representation and the json representation are not identical.
 */
object DecodingCoproduct extends App {
  /*
    For instance, if you have to model ADT coproduct type like the 4 houses in Hogwarts.
   */
  case class Houses(`type`: HouseType)

  sealed trait HouseType
  case object GodricGryffindor extends HouseType
  case object SalazarSlyntherin extends HouseType
  case object RowenaRavenclaw extends HouseType
  case object HelgaHufflepuff extends HouseType

  /**
  *   However, the input type from Json is a snake_case, and we need to convert them to PascalCase
   *   Define the implicit encoder and decoder for converting to a different type
   */
  implicit val housesEncoder: Encoder[HouseType] = (obj: HouseType) => obj match {
    case HelgaHufflepuff => Json.fromString("Helga_Hufflepuff")
    case RowenaRavenclaw => Json.fromString("Rowena_Ravenclaw")
    case GodricGryffindor => Json.fromString("Godric_Gryffindor")
    case SalazarSlyntherin => Json.fromString("Salazar_Slyntherin")
  }

  implicit val housesDecoder: Decoder[HouseType] = (hcursor:HCursor) => for {
    value <- hcursor.as[String]
    result <- value match {
      case "Helga_Hufflepuff" => HelgaHufflepuff.asRight
      case "Rowena_Ravenclaw" => RowenaRavenclaw.asRight
      case "Godric_Gryffindor" => GodricGryffindor.asRight
      case "Salazar_Slyntherin" => SalazarSlyntherin.asRight
      case s => DecodingFailure(s"Invalid house type ${s}", hcursor.history).asLeft
    }
  } yield result


  implicit val houseEncoder:Encoder[Houses] = deriveEncoder

  implicit val houseDecoder:Decoder[Houses] = deriveDecoder

  val gryffindor = (Houses(`type` = GodricGryffindor)).asJson
  println(gryffindor.spaces2)


}
