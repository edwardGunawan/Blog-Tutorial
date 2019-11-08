import cats.Traverse
import io.circe.{Decoder, HCursor, parser}

import cats.implicits._
import io.circe.generic.semiauto._

/*
Decoding Sealed Type
 */

sealed trait PaymentType
case class IDeal() extends PaymentType
case class CREDIT_CARD() extends PaymentType
case class PAYPAL() extends PaymentType
case class BRAINTREE() extends PaymentType
case class STRIPE() extends PaymentType

case class Error(value: String)

object PaymentType {

  def fromString(paymentType: String): Either[Error, PaymentType] = {
    paymentType.toLowerCase match {
      case "ideal" => Right(IDeal.apply())
      case "credit_card" => Right(CREDIT_CARD.apply())
      case "paypal" => Right(PAYPAL.apply())
      case "braintree" => Right(BRAINTREE.apply())
      case "stripe" => Right(STRIPE.apply())
      case _ => Left(Error("Unknown"))
    }
  }

  implicit val decoder: Decoder[Either[Error, PaymentType]] = (hCursor: HCursor) => {
    hCursor.as[String].map(paymentTypeString => PaymentType.fromString(paymentTypeString))
  }

  def main(args: Array[String]): Unit = {
    val inputString =
      """
        |[
        | "braintree", "paypal", "credit_card"
        |]
        |""".stripMargin

    // This becomes Either[List[Either[Error,PaymentType]]]
    parser.decode[List[Either[Error, PaymentType]]](inputString) match {
      case Right(paymentTypeListOfEither) => {
        // converting List[Either[Error,PaymentType]] to Either[List[PaymentType]], using cats
        val value = Traverse[List].traverse(paymentTypeListOfEither)(paymentType => paymentType)
        value match {
          case Right(paymentTypesList) => paymentTypesList.map(println)
          case Left(e) => println(s"something is wrong after parsing all the value ${e}")
        }
      }
      case Left(ex) => println(s"ooops decode failed ${ex}")
    }
  }
}
