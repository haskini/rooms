package com.github.api

import akka.actor.ActorRef
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.{delete, get, post}
import akka.pattern.ask
import com.github.common._
import com.github.services.UserActor._
import org.json4s._
import org.json4s.native.JsonMethods._

import scala.concurrent.Future
import scala.util.{Failure, Success}

trait UserRoutes {
  this: Routing =>
  
  // TODO: Handle entity errors
  lazy val userRoutes: Route =
    pathPrefix("user") {
      pathEnd {
        (get & checkAuth) { maybeJwt =>
          log.info("[GET] /user")
          maybeJwt match {
            case Left(error) =>
              completeWithLog(Errors.signedOut, StatusCodes.Unauthorized)
            case Right(jwt) =>
              parameters('email.?) { input =>
                val email = input match {
                  case Some(param) => param
                  case None => jwt.email
                }
                val answer = userActor ? GetUser(InModels.GetUser(email))
                val userFuture: Future[OutModels.GetUser] = answer.mapTo[OutModels.GetUser]
                onComplete(userFuture) {
                  case Success(user) =>
                    completeWithLog(user, StatusCodes.OK)
                  case Failure(_) =>
                    val messageFuture: Future[OutModels.MessageWithCode] =
                      answer.mapTo[OutModels.MessageWithCode]
                    onComplete(messageFuture) {
                      case Success(msg) =>
                        completeWithLog(msg, StatusCodes.OK)
                      case Failure(failure) =>
                        completeWithLog(Errors.unknown(failure.getMessage), StatusCodes.InternalServerError, isError = true)
                    }
                }
              }
          }
        } ~ (post & checkAuth) { maybeJwt =>
          log.info("[POST] /user")
          maybeJwt match {
            case Left(error) =>
              completeWithLog(Errors.signedOut, StatusCodes.Unauthorized)
            case Right(_) =>
              entity(as[String]) { data =>
                parse(data).extractOpt[InModels.CreateUser] match {
                  case Some(input) =>
                    val result: Future[OutModels.MessageWithCode] =
                      (userActor ? CreateUser(input)).mapTo[OutModels.MessageWithCode]
                    onComplete(result) {
                      case Success(msg) =>
                        msg match {
                          case OutModels.Error(_, _) =>
                            completeWithLog(msg, StatusCodes.BadRequest)
                          case OutModels.Message(_, _) =>
                            setJwt(JwtModel(
                              email = input.email,
                              name = input.name,
                              isAdmin = input.isAdmin
                            )) {
                              completeWithLog(msg, StatusCodes.Created)
                            }
                        }
                      case Failure(failure) =>
                        completeWithLog(failure.toString, StatusCodes.InternalServerError, isError = true)
                    }
                  case None =>
                    completeWithLog(Errors.invalidJson, StatusCodes.BadRequest)
                }
              }
          }
        } ~ (put & checkAuth) { maybeJwt =>
          log.info("[PUT] /user")
          maybeJwt match {
            case Left(error) =>
              completeWithLog(Errors.signedOut, StatusCodes.Unauthorized)
            case Right(jwt) =>
              entity(as[String]) { data =>
                parse(data).extractOpt[InModels.UpdateUser] match {
                  case Some(input) =>
                    val result: Future[OutModels.MessageWithCode] =
                      (userActor ? UpdateUser(jwt, input)).mapTo[OutModels.MessageWithCode]
                    onComplete(result) {
                      case Success(msg) =>
                        msg match {
                          case OutModels.Error(_, _) =>
                            completeWithLog(msg, StatusCodes.BadRequest)
                          case OutModels.Message(_, _) =>
                            setJwt(JwtModel(
                              email = input.email,
                              name = input.name,
                              isAdmin = input.isAdmin
                            )) {
                              completeWithLog(msg, StatusCodes.Created)
                            }
                        }
                      case Failure(failure) =>
                        completeWithLog(failure.toString, StatusCodes.InternalServerError, isError = true)
                    }
                  case None =>
                    completeWithLog(Errors.invalidJson, StatusCodes.BadRequest)
                }
              }
          }
        } ~ (delete & checkAuth) { maybeJwt =>
          log.info("[DELETE] /user")
          maybeJwt match {
            case Left(error) =>
              completeWithLog(Errors.signedOut, StatusCodes.Unauthorized)
            case Right(jwt) =>
              entity(as[String]) { data =>
                parse(data).extractOpt[InModels.DeleteUser] match {
                  case Some(input) =>
                    val result: Future[OutModels.MessageWithCode] =
                      (userActor ? DeleteUser(jwt, input)).mapTo[OutModels.MessageWithCode]
                    onComplete(result) {
                      case Success(msg) =>
                        msg match {
                          case OutModels.Error(_, _) =>
                            completeWithLog(msg, StatusCodes.BadRequest)
                          case OutModels.Message(_, _) =>
                            resetJwt() {
                              completeWithLog(msg, StatusCodes.OK)
                            }
                        }
                      case Failure(failure) =>
                        completeWithLog(failure.toString, StatusCodes.InternalServerError, isError = true)
                    }
                  case None =>
                    completeWithLog(Errors.invalidJson, StatusCodes.BadRequest)
                }
              }
          }
        }
      }
    } ~ pathPrefix("users") {
      pathEnd {
        (get & checkAuth) { maybeJwt =>
          log.info("[GET] /users")
          maybeJwt match {
            case Left(error) =>
              completeWithLog(Errors.signedOut, StatusCodes.Unauthorized)
            case Right(_) =>
              parameters('page.as[Int] ? 1, 'limit.as[Int] ? 10) { (page, limit) =>
                val result: Future[OutModels.GetUsers] =
                  (userActor ? GetUsers(InModels.GetUsers(page, limit))).mapTo[OutModels.GetUsers]
                onComplete(result) {
                  case Success(users) =>
                    completeWithLog(users, StatusCodes.OK)
                  case Failure(failure) =>
                    completeWithLog(failure.toString, StatusCodes.InternalServerError, isError = true)
                }
              }
          }
        }
      }
    } ~ pathPrefix("session") {
      pathEnd {
        (get & checkAuth) { maybeJwt =>
          log.info("[GET] /session")
          maybeJwt match {
            case Right(_) => completeWithLog(Messages.signedIn, StatusCodes.OK)
            case Left(error) =>
              completeWithLog(Messages.signedOut, StatusCodes.Unauthorized)
          }
        } ~ (post & checkAuth) { maybeJwt =>
          log.info("[POST] /session")
          maybeJwt match {
            case Right(_) => completeWithLog(Errors.signedIn, StatusCodes.BadRequest)
            case Left(error) =>
              entity(as[String]) { data =>
                parse(data).extractOpt[InModels.CheckPassword] match {
                  case Some(input) =>
                    val answer = userActor ? CheckPassword(input)
                    val userFuture: Future[OutModels.GetUser] = answer.mapTo[OutModels.GetUser]
                    onComplete(userFuture) {
                      case Success(user) =>
                        setJwt(JwtModel(
                          email = user.email,
                          name = user.name,
                          isAdmin = user.isAdmin
                        )) {
                          completeWithLog(Messages.ok, StatusCodes.OK)
                        }
                      case Failure(_) =>
                        val messageFuture: Future[OutModels.MessageWithCode] =
                          answer.mapTo[OutModels.MessageWithCode]
                        onComplete(messageFuture) {
                          case Success(msg) =>
                            completeWithLog(msg, StatusCodes.BadRequest)
                          case Failure(failure) =>
                            completeWithLog(failure.toString, StatusCodes.InternalServerError, isError = true)
                        }
                    }
                  case None =>
                    completeWithLog(Errors.invalidJson, StatusCodes.BadRequest)
                }
              }
          }
        } ~ (delete & checkAuth) { maybeJwt =>
          log.info("[DELETE] /session")
          maybeJwt match {
            case Left(error) =>
              completeWithLog(Errors.signedOut, StatusCodes.Unauthorized)
            case Right(_) => resetJwt() {
              completeWithLog(Messages.signedOut, StatusCodes.OK)
            }
          }
        }
      }
    }
  
  // other dependencies that UserRoutes use
  def userActor: ActorRef
}
