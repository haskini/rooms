package com.github.api

import akka.actor.{ActorRef, ActorSystem}
import akka.event.Logging
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.headers.HttpCookie
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.{delete, get, post}
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.pattern.ask
import akka.util.Timeout
import com.github.common._
import com.github.services.UserActor._
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization.write

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}

trait UserRoutes {
  this: Routing =>
  
  // TODO: Handle entity errors
  lazy val userRoutes: Route =
    concat(
      pathPrefix("user") {
        concat(
          pathEnd {
            concat(
              get {
                log.info("[GET] /user")
                if (!checkAuth()) {
                  val outJson = write(Errors.signedOut)
                  log.debug(outJson)
                  complete((StatusCodes.Unauthorized, outJson))
                }
                else
                  parameters('email.?) { input =>
                    var email = ""
                    input match {
                      case Some(param) => email = param
                      case None => email = Jwt.email
                    }
                    val answer = userActor ? GetUser(InModels.GetUser(email))
                    val userFuture: Future[OutModels.GetUser] = answer.mapTo[OutModels.GetUser]
                    onComplete(userFuture) {
                      case Success(user) =>
                        val outJson = write(user)
                        log.debug(outJson)
                        complete((StatusCodes.OK, outJson))
                      case Failure(_) =>
                        val messageFuture: Future[OutModels.MessageWithCode] =
                          answer.mapTo[OutModels.MessageWithCode]
                        onComplete(messageFuture) {
                          case Success(user) =>
                            val outJson = write(user)
                            log.debug(outJson)
                            complete((StatusCodes.OK, outJson))
                          case Failure(failure) =>
                            val outJson = write(failure)
                            log.error(outJson)
                            complete((StatusCodes.InternalServerError, outJson))
                        }
                    }
                  }
              },
              post {
                log.info("[POST] /user")
                if (!checkAuth()) {
                  val outJson = write(Errors.signedOut)
                  log.debug(outJson)
                  complete((StatusCodes.Unauthorized, outJson))
                }
                else
                  entity(as[String]) { data =>
                    parse(data).extractOpt[InModels.CreateUser] match {
                      case Some(input) =>
                        val result: Future[OutModels.MessageWithCode] =
                          (userActor ? CreateUser(input)).mapTo[OutModels.MessageWithCode]
                        onComplete(result) {
                          case Success(msg) =>
                            msg match {
                              case OutModels.Error(_, _) =>
                                val outJson = write(msg)
                                log.debug(outJson)
                                complete((StatusCodes.BadRequest, outJson))
                              case OutModels.Message(_, _) =>
                                // TODO: Generate JWT
                                val jwtToken = "here_will_be_jwt"
                                setCookie(HttpCookie(jwtCookieName, value = jwtToken)) {
                                  val outJson = write(msg)
                                  log.debug(outJson)
                                  complete((StatusCodes.Created, outJson))
                                }
                            }
                          case Failure(failure) =>
                            val outJson = write(failure)
                            log.error(outJson)
                            complete((StatusCodes.InternalServerError, outJson))
                        }
                      case None => complete((StatusCodes.BadRequest, "Incorrect json!"))
                    }
                  }
              },
              put {
                log.info("[PUT] /user")
                if (!checkAuth()) {
                  val outJson = write(Errors.signedOut)
                  log.debug(outJson)
                  complete((StatusCodes.Unauthorized, outJson))
                }
                else
                  entity(as[String]) { data =>
                    parse(data).extractOpt[InModels.UpdateUser] match {
                      case Some(input) =>
                        val result: Future[OutModels.MessageWithCode] =
                          (userActor ? UpdateUser(input)).mapTo[OutModels.MessageWithCode]
                        onComplete(result) {
                          case Success(msg) =>
                            msg match {
                              case OutModels.Error(_, _) =>
                                val outJson = write(msg)
                                log.debug(outJson)
                                complete((StatusCodes.BadRequest, outJson))
                              case OutModels.Message(_, _) =>
                                // TODO: Generate JWT
                                val jwtToken = "here_will_be_jwt"
                                setCookie(HttpCookie(jwtCookieName, value = jwtToken)) {
                                  val outJson = write(msg)
                                  log.debug(outJson)
                                  complete((StatusCodes.Created, outJson))
                                }
                            }
                          case Failure(failure) =>
                            val outJson = write(failure)
                            log.error(outJson)
                            complete((StatusCodes.InternalServerError, outJson))
                        }
                      case None => complete((StatusCodes.BadRequest, "Incorrect json!"))
                    }
                  }
              },
              delete {
                log.info("[DELETE] /user")
                if (!checkAuth()) {
                  val outJson = write(Errors.signedOut)
                  log.debug(outJson)
                  complete((StatusCodes.Unauthorized, outJson))
                }
                else
                  entity(as[String]) { data =>
                    parse(data).extractOpt[InModels.DeleteUser] match {
                      case Some(input) =>
                        val result: Future[OutModels.MessageWithCode] =
                          (userActor ? DeleteUser(input)).mapTo[OutModels.MessageWithCode]
                        onComplete(result) {
                          case Success(msg) =>
                            msg match {
                              case OutModels.Error(_, _) =>
                                val outJson = write(msg)
                                log.debug(outJson)
                                complete((StatusCodes.BadRequest, outJson))
                              case OutModels.Message(_, _) =>
                                deleteCookie(jwtCookieName) {
                                  val outJson = write(msg)
                                  log.debug(outJson)
                                  complete((StatusCodes.OK, outJson))
                                }
                            }
                          case Failure(failure) =>
                            val outJson = write(failure)
                            log.error(outJson)
                            complete((StatusCodes.InternalServerError, outJson))
                        }
                      case None => complete((StatusCodes.BadRequest, "Incorrect json!"))
                    }
                  }
              },
            )
          },
        )
      },
      pathPrefix("users") {
        concat(
          pathEnd {
            concat(
              get {
                log.info("[GET] /users")
                if (!checkAuth()) {
                  val outJson = write(Errors.signedOut)
                  log.debug(outJson)
                  complete((StatusCodes.Unauthorized, outJson))
                }
                else
                  parameters('page.as[Int] ? 1, 'limit.as[Int] ? 10) { (page, limit) =>
                    val result: Future[OutModels.GetUsers] =
                      (userActor ? GetUsers(InModels.GetUsers(page, limit))).mapTo[OutModels.GetUsers]
                    onComplete(result) {
                      case Success(users) =>
                        val outJson = write(users)
                        log.debug(outJson)
                        complete((StatusCodes.OK, outJson))
                      case Failure(failure) =>
                        val outJson = write(failure)
                        log.error(outJson)
                        complete((StatusCodes.InternalServerError, outJson))
                    }
                  }
              },
            )
          },
        )
      },
      pathPrefix("session") {
        concat(
          pathEnd {
            concat(
              get {
                log.info("[GET] /session")
                if (checkAuth()) {
                  val outJson = write(Messages.signedIn)
                  log.debug(outJson)
                  complete((StatusCodes.OK, outJson))
                }
                else {
                  val outJson = write(Messages.signedOut)
                  log.debug(outJson)
                  complete((StatusCodes.Unauthorized, outJson))
                }
              },
              post {
                log.info("[POST] /session")
                if (!checkAuth()) {
                  val outJson = write(Errors.signedIn)
                  log.debug(outJson)
                  complete((StatusCodes.BadRequest, outJson))
                }
                else
                  entity(as[String]) { data =>
                    parse(data).extractOpt[InModels.CheckPassword] match {
                      case Some(input) =>
                        val result: Future[OutModels.MessageWithCode] =
                          (userActor ? CheckPassword(input)).mapTo[OutModels.MessageWithCode]
                        onComplete(result) {
                          case Success(msg) =>
                            msg match {
                              case OutModels.Error(code, _) =>
                                val outJson = write(msg)
                                log.debug(outJson)
                                complete((StatusCodes.BadRequest, outJson))
                              case OutModels.Message(_, _) =>
                                // TODO: Generate JWT
                                val jwtToken = "here_will_be_jwt"
                                setCookie(HttpCookie(jwtCookieName, value = jwtToken)) {
                                  val outJson = write(msg)
                                  log.debug(outJson)
                                  complete((StatusCodes.OK, outJson))
                                }
                            }
                          case Failure(failure) =>
                            val outJson = write(failure)
                            log.error(outJson)
                            complete((StatusCodes.InternalServerError, outJson))
                        }
                      case None => complete((StatusCodes.BadRequest, "Incorrect json!"))
                    }
                  }
              },
              delete {
                log.info("[DELETE] /session")
                if (!checkAuth()) {
                  val outJson = write(Errors.signedOut)
                  log.debug(outJson)
                  complete((StatusCodes.Unauthorized, outJson))
                }
                else {
                  deleteCookie(jwtCookieName) {
                    val outJson = write(Messages.signedOut)
                    log.debug(outJson)
                    complete((StatusCodes.OK, outJson))
                  }
                }
              },
            )
          },
        )
      },
    )
  
  // other dependencies that UserRoutes use
  def userActor: ActorRef
}
