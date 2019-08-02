package com.github.api

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.model.headers.HttpCookie
import akka.http.scaladsl.model.{DateTime, StatusCode, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.http.scaladsl.server.{Directive, Directive0, Directive1, Route, StandardRoute}
import akka.util.Timeout
import com.github.common.{ErrorType, Errors, InModels, JwtInvalid, JwtModel}
import org.json4s.DefaultFormats
import org.json4s.native.JsonMethods.parse
import org.json4s.native.Serialization.write
import pdi.jwt.{JwtAlgorithm, JwtClaim, JwtJson4s}

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
  lazy val jwtLifetime: Long = DateTime(1970, 2, 1).clicks
  lazy val jwtAlgo = JwtAlgorithm.HS256
  
  def completeWithLog[A <: AnyRef](data: A, code: StatusCode, isError: Boolean = false): Route = {
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
  
  def checkAuth: Directive1[Option[JwtModel]] = {
    optionalCookie(jwtCookieName) map {
      case Some(cookie) =>
        val claim: JwtClaim = JwtJson4s.decode(cookie.value, jwtSecret, Seq(jwtAlgo)).getOrElse(JwtClaim())
        getJwtData(claim.content) match {
          case Some(jwt) => Option(jwt)
          case None => None
        }
      case None => None
    }
  }
  
  def getJwtData(data: String): Option[JwtModel] = {
    if (data.isEmpty)
      None
    else
      parse(data).extractOpt[JwtModel]
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
