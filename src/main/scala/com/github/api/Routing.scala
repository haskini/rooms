package com.github.api

import akka.actor.ActorSystem
import akka.event.Logging
import akka.util.Timeout
import org.json4s.DefaultFormats

import scala.concurrent.duration._

trait Routing {
  // we leave these abstract, since they will be provided by the App
  implicit def system: ActorSystem
  // Formats for json parsing
  implicit val formats: DefaultFormats.type = DefaultFormats
  // Required by the `ask` (?) method below
  implicit lazy val timeout: Timeout = Timeout(5.seconds) // usually we'd obtain the timeout from the system's configuration
  // Logs
  lazy val log = Logging(system, classOf[Routing])
  // Cookie name for JWT
  lazy val jwtCookieName: String = "jwt"
  
  def checkAuth(): Boolean = {
    //optionalCookie(jwtCookieName) {
    //  case Some(cookie) =>
    //    // TODO: Check JWT
    //    //if (checkJwt(cookie.value)) {
    //    if (true)
    //      true
    //    else {
    //      deleteCookie(jwtCookieName) {
    //        false
    //      }
    //    }
    //  case None =>
    //    false
    //}
    true
  }
}
