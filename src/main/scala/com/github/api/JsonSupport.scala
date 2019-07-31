package com.github.api

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.github.common.UserActor.ActionPerformed
import com.github.common.{GetUserOut, GetUsersOut}
import spray.json.DefaultJsonProtocol

trait JsonSupport extends SprayJsonSupport {
    // import the default encoders for primitive types (Int, String, Lists etc)
    import DefaultJsonProtocol._
    
    implicit val userJsonFormat = jsonFormat4(GetUserOut)
    implicit val usersJsonFormat = jsonFormat1(GetUsersOut)
    
    implicit val actionPerformedJsonFormat = jsonFormat1(ActionPerformed)
}
