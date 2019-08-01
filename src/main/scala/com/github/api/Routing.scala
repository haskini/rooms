package com.github.api

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.model.headers.HttpCookie
import akka.http.scaladsl.model.{DateTime, StatusCode}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.http.scaladsl.server.{Directive0, StandardRoute}
import akka.util.Timeout
import com.github.common.JwtModel
import org.json4s.DefaultFormats
import org.json4s.native.Serialization.write
import pdi.jwt.{JwtAlgorithm, JwtClaim, JwtJson4s}
import org.json4s.JsonDSL.WithBigDecimal._
import org.json4s.native.JsonMethods._
import pdi.jwt.{JwtAlgorithm, JwtJson4s}

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
  // JWT
  lazy val jwtCookieName: String = "jwt"
  lazy val jwtSecret: String = "very_secret_key"
  lazy val jwtLifetime: Long = 1000 * 60 * 60 * 24 * 30
  lazy val jwtAlgo = JwtAlgorithm.HS256
  
  def completeWithLog[A <: AnyRef](data: A, code: StatusCode, isError: Boolean = false): StandardRoute = {
    val outJson = write(data)
    if (!isError) {
      log.debug(outJson)
      complete((code, outJson))
    }
    else {
      log.error(outJson)
      complete((code, outJson))
    }
  }
  
  val jwt = JwtModel(
    email = "test@mail.ru",
    name = "Test",
    isAdmin = true
  )
  
  def checkAuth(): Boolean = {
    //    optionalCookie(jwtCookieName) {
    //      case Some(cookie) =>
    //        complete(cookie.value)
    //    // TODO: Check JWT
    //    //if (checkJwt(cookie.value)) {
    //    if (true)
    //      true
    //    else {
    //      deleteCookie(jwtCookieName) {
    //        false
    //      }
    //    }
    //      case None =>
    //          false
    //    }
    true
  }
  
  def generateJwt(data: JwtModel, expires: DateTime): String = {
    JwtJson4s.encode(JwtClaim(
      content = write(data),
      expiration = Option(expires.clicks)
    ), jwtSecret, jwtAlgo)
  }
  
  def setJwt(data: JwtModel): Directive0 = {
    setCookie(HttpCookie(
      name = jwtCookieName,
      value = generateJwt(JwtModel(
        email = data.email,
        name = data.name,
        isAdmin = data.isAdmin,
      ), DateTime.now.plus(jwtLifetime)),
      httpOnly = true,
      expires = Option(DateTime.now.plus(jwtLifetime))
    ))
  }
  
  def resetJwt(): Directive0 = {
    deleteCookie(jwtCookieName)
  }
}
