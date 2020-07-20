import sbt._

object Dependencies {

  object Scala {
    val v12 = "2.12.10"
    val v13 = "2.13.1"
  }

  object AWSSDKV2 {
    private val version = "2.9.22"
    val core:ModuleID = "software.amazon.awssdk" % "core" % version
  }

  object AWSJavaLambda {
    private val version = "1.2.0"
    val lambdaJavaCore:ModuleID = "com.amazonaws" % "aws-lambda-java-core" % version
  }

  object Circe {
    private val version = "0.13.0"
    private val genericExtraVersion = "0.12.2"

    val core:ModuleID = "io.circe" %% "circe-core" % version
    val generic:ModuleID = "io.circe" %% "circe-generic" % version
    val parser:ModuleID = "io.circe" %% "circe-parser" % version
    val literal:ModuleID = "io.circe" %% "circe-literal" % version
    val genericExtra: ModuleID = "io.circe" %% "circe-generic-extras" % genericExtraVersion

  }

  object Cats {
    private val version = "2.1.1"
    val core:ModuleID = "org.typelevel" %% "cats-core" % version
    val effect:ModuleID = "org.typelevel" %% "cats-effect"% version
  }




  // for testing
  object ScalaTest {
    private val version = "3.2.0"
    val scalaTest:ModuleID = "org.scalatest" %% "scalatest" % version
  }


}
