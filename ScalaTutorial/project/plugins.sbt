resolvers += "bintray-sbt-plugins" at "https://dl.bintray.com/rtimush/sbt-plugin-snapshots/" 
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.6")
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.2.0")
addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full)