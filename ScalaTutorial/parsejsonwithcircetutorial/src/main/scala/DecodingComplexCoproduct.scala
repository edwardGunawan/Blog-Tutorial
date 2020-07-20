
import DecodingComplexCoproduct.HousesTypes.{GodricGryffindor, HelgaHufflepuff, RowenaRavenclaw, SalazarSlyntherin}
import DecodingCoproduct.Houses
import io.circe.{Decoder, DecodingFailure, Encoder, HCursor, Json}
import cats.implicits._
import io.circe.generic.JsonCodec
import io.circe.generic.extras.Configuration
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.syntax._
import io.circe.parser._
/*
  Decoding complex coproduct that will need to change its constructor names to one of the `type` class.

  Making Constructor name as polymorphic.

  For instance, having a Json value that has a type like this:
    {
      "characteristics": ["brave at heart"],
      "type": "Gryffindor"
    }

   To something like this:
   Gryffindor(characteristics: List[String])

   Based on the type we want to polymorphically decode to the write case class.

    If you do the regular Circe deriveEncoderConfiguredEncoder, deriveConfiguredDecoder without any change in configuration,
    you will get an encode something like this:
    Gryffindor(characteristics: List[String],

 */
object DecodingComplexCoproduct extends App {

  @JsonCodec
  case class House(houseType: HousesTypes, number:Int)

  /** Method 1: manually decode the ADT
  *  The way that you structure the ADT will be different
   */
  trait HousesTypes

  object HousesTypes {
    @JsonCodec
    case class GodricGryffindor(characteristics:List[String]) extends HousesTypes

    object GodricGryffindor{
      val typeId: String = "Godric_Gryffindor"
    }


    case object SalazarSlyntherin extends HousesTypes {
      val typeId: String = "Salazar_Slyntherin"
    }

    @JsonCodec
    case class RowenaRavenclaw(characteristics:List[String], animalRepresentation:String) extends HousesTypes

    object RowenaRavenclaw{
       val typeId: String = "Rowena_Ravenclaw"
    }

    @JsonCodec
    case class HelgaHufflepuff(animalRepresentation:String, colours:String) extends HousesTypes

    object HelgaHufflepuff{
      val typeId: String = "Helga_Hufflepuff"
    }

    implicit val decoder:Decoder[HousesTypes] = (cursor:HCursor) => for {
      tpe <- cursor.get[String]("type")
      result <- tpe match {
        case GodricGryffindor.typeId => cursor.as[GodricGryffindor]
        case RowenaRavenclaw.typeId => cursor.as[RowenaRavenclaw]
        case HelgaHufflepuff.typeId => cursor.as[HelgaHufflepuff]
        case SalazarSlyntherin.typeId => SalazarSlyntherin.asRight
        case s => DecodingFailure(s"Invalid house type ${s}", cursor.history).asLeft
      }
    } yield result

    implicit val encoder:Encoder[HousesTypes] =  {
      // deepMerge - insert the encoded Json with another field `type`
      // Basically overriding the current encoder with the `type`
      case obj: GodricGryffindor => obj.asJson deepMerge(Json.obj("type" -> Json.fromString(GodricGryffindor.typeId)))
      case obj: RowenaRavenclaw => obj.asJson deepMerge(Json.obj("type" -> Json.fromString(RowenaRavenclaw.typeId)))

      case obj: HelgaHufflepuff => obj.asJson deepMerge(Json.obj("type" -> Json.fromString(HelgaHufflepuff.typeId)))
      case obj: HousesTypes => Json.obj("type" -> Json.fromString(SalazarSlyntherin.typeId))
    }
  }


  // RavenClaw
  val ravenClaw: HousesTypes = RowenaRavenclaw(characteristics = List("Loyal"), animalRepresentation = "eagle")
  val ravenClawJson = ravenClaw.asJson
  val ravenClawStr = ravenClawJson.noSpaces
  println(ravenClaw.asJson.spaces2)
  println(decode[HousesTypes](ravenClawStr).right.get)

  val godricGryffindor: HousesTypes = GodricGryffindor(characteristics = List("ruthless"))
  val godricGryffindorJson = godricGryffindor.asJson
  val godricGryffindorStr = godricGryffindorJson.noSpaces
  println(godricGryffindorJson.spaces2)
  println(decode[HousesTypes](godricGryffindorStr).right.get)

  val helgaHufflepuff: HousesTypes = HelgaHufflepuff(animalRepresentation = "badger", colours = "yellow")
  val helgaHufflepuffJson = helgaHufflepuff.asJson
  val helgaHufflepuffStr = helgaHufflepuffJson.noSpaces
  println(helgaHufflepuffJson.spaces2)
  println(decode[HousesTypes](helgaHufflepuffStr).right.get)

  val slyntherin: HousesTypes = SalazarSlyntherin
  val slyntherinStr = slyntherin.asJson.spaces2
  println(slyntherin.asJson.spaces2)
  println(decode[HousesTypes](slyntherinStr).right.get)

  val housesJson = House(houseType = ravenClaw,number = 12).asJson
  println(housesJson.spaces2)
  println(decode[House](housesJson.spaces2).right.get)







//  // decode to House2
//  decode[House2](str) match {
//    case Left(ex) => println(s"something goes wrong on decoding $str , $ex")
//    case Right(ravenClaw) => println(s"successfully decode to House2 $ravenClaw")
//  }









}
