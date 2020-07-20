import java.time.Instant

import io.circe.{Decoder, Encoder, Json}
import io.circe.generic.semiauto._
import io.circe.syntax._
import io.circe.parser._
import cats.implicits._

/**
 * Use case:
 * Having a case class Currency that has the createdDate as a EpochLong. However, we want to convert those epoch long to a Instant type
 * because it is easier to do any processing, to print the value and do any sort of time manipulation.
 */
object EncodingDecodingInstant extends App {

  case class Currency(id: Int, name:String, description:String, isoCodeAlphabetic:String, createdDate:Instant)

  object Currency {
    // transforming the Long Epoch Milli to an Instant type. This works because it will check the scope when trying to decode Long, and will use that instance
    implicit val encoder:Encoder[Instant] = Encoder.instance(time => Json.fromLong(time.toEpochMilli))
    // emap create a decoder that performs some operation if this one succeed
    implicit val decoder:Decoder[Instant] = Decoder.decodeLong.emap(l => Either.catchNonFatal(Instant.ofEpochMilli(l)).leftMap(t => "Instant"))

    implicit val encoderCurrency: Encoder[Currency] = deriveEncoder
    implicit val deoderCurrency: Decoder[Currency] = deriveDecoder
  }

  val currency = Currency(id = 1,name = "US Dollars",description = "United States Dollar",isoCodeAlphabetic = "USD",createdDate = Instant.now)
  val currencyJson = currency.asJson
  println(currencyJson.spaces2)

  val currencyStr = """{
               |  "id" : 1,
               |  "name" : "US Dollars",
               |  "description" : "United States Dollar",
               |  "isoCodeAlphabetic" : "USD",
               |  "createdDate" : 1595270691417
               |}""".stripMargin

  println(decode[Currency](currencyStr).right.get)









}
