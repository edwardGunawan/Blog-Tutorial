package example
import com.amazonaws.services.lambda.runtime.{Context, RequestStreamHandler}
import java.io.InputStream
import java.io.OutputStream
import scala.io.Source

import io.circe.parser._
import io.circe.syntax._

class MainHandler extends RequestStreamHandler {
  override def handleRequest(input: InputStream, output: OutputStream, context: Context): Unit = {
    val inputString: String = Source.fromInputStream(input).mkString

    decode[RequestFormatter](inputString).map(requestFormatter => {
      println(s"${requestFormatter} is here")
      // do something .....

      output.write(requestFormatter.asJson.noSpaces.toCharArray.map(_.toByte))
    })
  }

}
