import cats.Traverse
import io.circe.Decoder.Result
import io.circe.{Decoder, DecodingFailure, HCursor, Json, parser}
import cats.implicits._

/**
  * When you create all the case classes, circe will automatically do the decoding for you for all those classes.
  * You just need to define how you want circe to traverse the documents, with hCursor,and then it will automatically,
  * polymorphically decode the next objects
  *
  * Lets say we are getting value from priceService that has an Array of Objects that has another array. And we want
  * to get one of the attributes inside the nested array objects. However, we want to assign this as a different name
  * in our class to further use.
  */
/*
  [
    { "name":"productResource", orderItems: [ { voucher: { "campaignNumber":12, "discount":20, "subscriptionPeriod" "June" }}, { voucher: { "campaignNumber":13, "discount":24 }}] },
    { "name":"productResource2", orderItems: [ { voucher: { "campaignNumber":13, "discount":24 }}] },
    { "name":"productResource3", orderItems: [ { voucher: { "campaignNumber":15, "discount":28 }}] }
  ]
 */

case class ProductResrsource(name: String, campaignResources: List[Int], discountPrice: List[Int])

object voucher {
  implicit val decoder: Decoder[ProductResrsource] = new Decoder[ProductResrsource] {
    override def apply(hCursor: HCursor): Result[ProductResrsource] =
      for {
        name <- hCursor.downField("name").as[String]
        orderItemsJson <- hCursor.downField("orderItems").as[List[Json]] // set it to list JSon but technically
        // because each itemJson downField will become List[Either[DecodeFailure, Int]] so we need to use cats to traverse List[Either] to Either[List]
        // each of as[Int] returns a Result[Int] and Result[Int] has a type of Either[DecodeFailure,A] which will become List[Either]
        campaignResource <- Traverse[List].traverse(orderItemsJson)(
          itemJson => itemJson.hcursor.downField("voucher").downField("campaignNumber").as[Int]
        )
        discountPrice <- Traverse[List].traverse(orderItemsJson)(orderItemsJson => {
          orderItemsJson.hcursor.downField("voucher").downField("discount").as[Int]
        })
      } yield {
        ProductResrsource(name, campaignResource, discountPrice)
      }
  }

  def main(args: Array[String]): Unit = {
    val inputString =
      """
        |[
        |   {
        |      "name":"productResource",
        |      "orderItems":[
        |         {
        |            "voucher":{
        |               "campaignNumber":12,
        |               "discount":20,
        |               "subscriptionPeriod":"June"
        |            }
        |         },
        |         {
        |            "voucher":{
        |               "campaignNumber":13,
        |               "discount":24
        |            }
        |         }
        |      ]
        |   },
        |   {
        |      "name":"productResource2",
        |      "orderItems":[
        |         {
        |            "voucher":{
        |               "campaignNumber":13,
        |               "discount":24
        |            }
        |         }
        |      ]
        |   },
        |   {
        |      "name":"productResource3",
        |      "orderItems":[
        |         {
        |            "voucher":{
        |               "campaignNumber":15,
        |               "discount":28
        |            }
        |         }
        |      ]
        |   }
        |]
        |""".stripMargin
    parser.decode[List[ProductResrsource]](inputString) match {
      case Right(vouchers) => vouchers.map(println)
      case Left(ex) => println(s"Something wrong ${ex}")
    }
  }
}
