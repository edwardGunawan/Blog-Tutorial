import Dependencies._

lazy val commonSettings = Seq (
//  scalacOptions ++= Seq("-Ypartial-unification", "-deprecation"),
  version := "0.1",
  scalaVersion := "2.13.1",
  assemblyJarName in assembly := s"${name}-${version}.jar",
  test in assembly := {} // not run tests at assembly,

)

lazy val awslambdascalatutorial = project.in(file("awslambdascalatutorial"))
  .settings(
    name := "AWS Lambda Scala Tutorial",
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

lazy val testingakkahttptutorial = project.in(file("testingakkahttptutorial"))
  .settings(
    name := "testing-akka-http-tutorial",
    commonSettings,
    libraryDependencies ++= Seq (
      "com.typesafe.akka" %% "akka-actor" % "2.6.0",
      "com.typesafe.akka" %% "akka-http"   % "10.1.10",
      "com.typesafe.akka" %% "akka-stream" % "2.6.0",
      "com.typesafe.akka" %% "akka-slf4j" % "2.6.0",
      "io.circe" %% "circe-core" % "0.12.3",
      "io.circe" %% "circe-generic" % "0.12.3",
      "io.circe" %% "circe-parser" % "0.12.3"
    ) ++ Seq(
      "org.scalamock" %% "scalamock" % "4.4.0",
      "org.scalatest" %% "scalatest" % "3.0.8",
      "com.typesafe.akka" %% "akka-testkit" % "2.6.0-M8"
    ).map(_%"test")
  )

  lazy val parsejsonwithcircetutorial = project.in(file("parsejsonwithcircetutorial"))
  .settings(
    name:= "Parse Json with Circe Tutorial",
    commonSettings,
    libraryDependencies ++= Seq (
      Circe.core,
      Circe.generic,
      Circe.parser,
      Cats.core
    ) ++ Seq (
      ScalaTest.scalaTest
    ).map(_ % "test")
  )

lazy val whatIsTypeClass = project.in(file("whatIsTypeClass"))
  .settings(
    name:= "What is a Type Class",
    commonSettings
  )


lazy val catsWriterType = project.in(file("catsWriterType"))
  .settings(
    name:= "Cats Writer Data Type Tutorial",
    commonSettings,
    libraryDependencies ++= Seq(
      Cats.core
    )
  )

lazy val catsReaderType = project.in(file("catsReaderType"))
  .settings(
    name:= "Cats Reader Data Type Tutorial",
    commonSettings,
    libraryDependencies ++= Seq(
      Cats.core
    )
  )

lazy val catsStateMonad = project.in(file("catsStateMonad"))
  .settings(
    name:= "Cats State Data Type",
    commonSettings,
    libraryDependencies ++= Seq(
      Cats.core
    )
  )

lazy val combinationTutorial = project.in(file("combinationTutorial"))
  .settings(
    name:= "Combination Tutorial",
    commonSettings
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
