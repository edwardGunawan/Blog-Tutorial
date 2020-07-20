import DecodingComplexCoProductWithCirceExtra.HouseType.{GodricGryffindor, HelgaHufflepuff, RowenaRavenclaw, SalazarSlyntherin}
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.{deriveConfiguredEncoder, deriveConfiguredDecoder}
import io.circe.syntax._
import io.circe.parser._

/*
  Decoding complex coproduct that will need to change its constructor names to one of the `type` class.

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
object DecodingComplexCoProductWithCirceExtra extends App {
  /**
    *  Using deriveEncoder/decoder with Circe Extras
    * The model will be created differently
    */
  sealed trait HouseType {
    def `type`: String
  }

  object HouseType {
    case class GodricGryffindor(characteristics:List[String]) extends HouseType {
      override def `type`: String = "Godric_Gryffindor"
    }
    case object SalazarSlyntherin extends HouseType {
      override def `type`: String = "Salazar_Slyntherin"
    }
    case class RowenaRavenclaw(characteristics:List[String], animalRepresentation:String) extends HouseType {
      override def `type`: String = "Rowena_Ravenclaw"
    }
    case class HelgaHufflepuff(animalRepresentation:String, colours:String) extends HouseType {
      override def `type`: String = "Helga_Hufflepuff"
    }

    /*
   Define the configuration by replacing the constructorNames.
  */
    lazy val houseTypeConfig = Configuration.default.withDiscriminator("type").copy(
      transformConstructorNames = {
        case "GodricGryffindor" => "Godric_Gryffindor" // from `type` on the right transform the case changes to the left
        case "SalazarSlyntherin" => "Salazar_Slyntherin"
        case "RowenaRavenclaw" => "Rowena_Ravenclaw"
        case "HelgaHufflepuff" => "Helga_Hufflepuff"
      }
    )

    /*
      Create a discriminator for `type` in the Json String, and make the constructor name as SnakeCase
      {
        "characteristics" : [
          "Loyal"
        ],
        "animalRepresentation" : "eagle",
        "type" : "rowena_ravenclaw" << snake case from the ctor name
      }
     */
//    lazy val houseTypeConfig = Configuration.default.withDiscriminator("type").withSnakeCaseConstructorNames

//    lazy val houseTypeConfig = Configuration.default.withDiscriminator("type").copy(
//      transformConstructorNames = {
//        case "Godric_Gryffindor" => "GodricGryffindor" // from `type` on the right transform the case changes to the left
//        case "Salazar_Slyntherin" => "SalazarSlyntherin"
//        case "Rowena_Ravenclaw" => "RowenaRavenclaw"
//        case "Helga_Hufflepuff" => "HelgaHufflepuff"
//      }
//    )

    /*
    Encode and decode with `deriveEncoder/Decoder` with circe.extras
     */
    implicit val house2Encoder = {
      implicit val config = houseTypeConfig
      deriveConfiguredEncoder[HouseType]
    }

    implicit val house2Decoder = {
      implicit val config = houseTypeConfig
      deriveConfiguredDecoder[HouseType]
    }
  }

  // RavenClaw
  val ravenClaw: HouseType = RowenaRavenclaw(characteristics = List("Loyal"), animalRepresentation = "eagle")
  val ravenClawJson = ravenClaw.asJson
  val ravenClawStr = ravenClawJson.noSpaces
  println(ravenClaw.asJson.spaces2)
  println(decode[HouseType](ravenClawStr).right.get)

  val godricGryffindor: HouseType = GodricGryffindor(characteristics = List("ruthless"))
  val godricGryffindorJson = godricGryffindor.asJson
  val godricGryffindorStr = godricGryffindorJson.noSpaces
  println(godricGryffindorJson.spaces2)
  println(decode[HouseType](godricGryffindorStr).right.get)

  val helgaHufflepuff: HouseType = HelgaHufflepuff(animalRepresentation = "badger", colours = "yellow")
  val helgaHufflepuffJson = helgaHufflepuff.asJson
  val helgaHufflepuffStr = helgaHufflepuffJson.noSpaces
  println(helgaHufflepuffJson.spaces2)
  println(decode[HouseType](helgaHufflepuffStr).right.get)

  val slyntherin: HouseType = SalazarSlyntherin
  val slyntherinStr = slyntherin.asJson.spaces2
  println(slyntherin.asJson.spaces2)
  println(decode[HouseType](slyntherinStr).right.get)
}
