name := "CloudPointCostManager"

version := "1.0"

scalaVersion := "2.11.8"

resolvers += "Typesafe Repo" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies += "com.typesafe.play" %% "play-json" % "2.5.8"
libraryDependencies += "com.typesafe.play" %% "play-ws" % "2.5.8"

libraryDependencies += "com.amazonaws" % "aws-lambda-java-core" % "1.1.0" % "provided"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.1.7"
libraryDependencies += "com.amazonaws" % "aws-java-sdk" % "1.10.15"

libraryDependencies += "org.apache.commons" % "commons-compress" % "1.5"

libraryDependencies += "com.lambdaworks" %% "jacks" % "2.3.3"

libraryDependencies ~= { _ map {
    case m =>
        m.exclude("commons-logging", "commons-logging")
            .exclude("com.typesafe.play", "build-link")
}}

mergeStrategy in assembly <<= (mergeStrategy in assembly) { old => {
    case e if e.endsWith("io.netty.versions.properties") => MergeStrategy.first
    case e => old(e)
}
}


