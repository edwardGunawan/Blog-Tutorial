import sbt._

object Dependencies {
  object AWSSDKV2 {
    private val version = "2.9.22"
    val core:ModuleID = "software.amazon.awssdk" % "core" % version
  }

  object AWSJavaLambda {
    private val version = "1.2.0"
    val lambdaJavaCore:ModuleID = "com.amazonaws" % "aws-lambda-java-core" % version
  }

  object Circe {
    private val version = "0.12.3"
    private val genericExtraVersion = "0.12.2"

    val core:ModuleID = "io.circe" %% "circe-core" % version
    val generic:ModuleID = "io.circe" %% "circe-generic" % version
    val parser:ModuleID = "io.circe" %% "circe-parser" % version
    val literal:ModuleID = "io.circe" %% "circe-literal" % version
    val genericExtra: ModuleID = "io.circe" %% "circe-generic-extras" % genericExtraVersion

  }

  object Cats {
    private val version = "2.0.0"
    val core:ModuleID = "org.typelevel" %% "cats-core" % version
    val effect:ModuleID = "org.typelevel" %% "cats-effect"% version
  }




  // for testing
  object ScalaTest {
    private val version = "3.0.8"
    val scalaTest:ModuleID = "org.scalatest" %% "scalatest" % version
  }


}
