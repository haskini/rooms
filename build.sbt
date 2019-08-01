lazy val akkaHttpVersion = "10.1.9"
lazy val akkaVersion = "2.6.0-M5"
lazy val mongoDriverVersion = "2.6.0"
//lazy val mongoCasbahVersion = "3.1.1"
lazy val json4sVersion = "3.6.7"
lazy val jwtVersion = "3.1.0"

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
            // "org.mongodb" %% "casbah-commons" % mongoCasbahVersion,
            
            "org.json4s" %% "json4s-native" % json4sVersion,
            "com.pauldijou" %% "jwt-json4s-native" % jwtVersion,
            
            "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test,
            "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
            "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % Test,
            "org.scalatest" %% "scalatest" % "3.0.5" % Test,
        )
    )
