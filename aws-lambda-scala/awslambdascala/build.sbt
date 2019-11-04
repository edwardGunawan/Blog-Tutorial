import Dependencies._

lazy val commonSettings = Seq (
  version := "0.1",
  organization := "com.example",
  scalaVersion := "2.13.1",
  assemblyJarName in assembly := s"${name}-${version}.jar",
  test in assembly := {} // not run tests at assembly
)

lazy val root = (project in file("."))
  .settings(
    name := "aws AlLambda Scala",
    commonSettings,
    libraryDependencies ++= Seq(
      AWSSDKV2.core,
      AWSJavaLambda.lambdaJavaCore,
      Circe.core,
      Circe.generic,
      Circe.parser,
      Circe.literal,
      Circe.genericExtra
    ) ++ Seq (
      ScalaTest.scalaTest
    ).map(_%"test")
  )

lazy val assemblySettings = Seq(
  assemblyJarName in assembly := name.value+".jar",
  assemblyMergeStrategy in assembly := {
    case PathList("MET_INF", _ @_*) => MergeStrategy.discard
    case "application.conf" => MergeStrategy.concat
    case "reference.conf" => MergeStrategy.concat
    case x => MergeStrategy.first
  }
)




resolvers += Resolver.typesafeIvyRepo("releases")
libraryDependencies += "com.lightbend" %% "emoji" % "1.2.1"


// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
