lazy val akkaHttpVersion = "10.1.9"
lazy val akkaVersion = "2.6.0-M5"
lazy val mongoDriverVersion = "2.6.0"
lazy val json4sVersion = "3.6.7"
lazy val jwtVersion = "3.1.0"
lazy val scalatestVersion = "3.0.5"
lazy val softwaremillVersion = "1.6.3"
lazy val EmbedmongoVersion = "0.2.4"

lazy val root = (project in file(".")).
    settings(
        inThisBuild(List(
            organization := "com.github",
            scalaVersion := "2.12.8"
        )),
        name := "rooms",
        libraryDependencies ++= Seq(
            "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
            "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
            "com.typesafe.akka" %% "akka-http-xml" % akkaHttpVersion,
            "com.typesafe.akka" %% "akka-stream" % akkaVersion,
            
            "org.mongodb.scala" %% "mongo-scala-driver" % mongoDriverVersion,
            
            "org.json4s" %% "json4s-native" % json4sVersion,
            "com.pauldijou" %% "jwt-json4s-native" % jwtVersion,
    
            "org.slf4j" % "slf4j-simple" % "1.6.2" % Test,
            "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test,
            "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
            "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % Test,
            "org.scalatest" %% "scalatest" % scalatestVersion % Test,
            "com.softwaremill.sttp" %% "core" % softwaremillVersion % Test,
            "com.github.simplyscala" %% "scalatest-embedmongo" % EmbedmongoVersion % Test
        ),
        parallelExecution in Test := false
    )
