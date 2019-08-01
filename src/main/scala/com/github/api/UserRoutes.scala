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
        get {
          log.info("[GET] /user")
          if (!checkAuth()) {
            completeWithLog(Errors.signedOut, StatusCodes.Unauthorized)
          }
          else
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
                      completeWithLog(failure, StatusCodes.InternalServerError, isError = true)
                  }
              }
            }
        } ~ post {
          log.info("[POST] /user")
          if (!checkAuth()) {
            completeWithLog(Errors.signedOut, StatusCodes.Unauthorized)
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
                      completeWithLog(failure, StatusCodes.InternalServerError, isError = true)
                  }
                case None =>
                  completeWithLog(Errors.invalidJson, StatusCodes.BadRequest)
              }
            }
        } ~ put {
          log.info("[PUT] /user")
          if (!checkAuth()) {
            completeWithLog(Errors.signedOut, StatusCodes.Unauthorized)
          }
          else
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
                      completeWithLog(failure, StatusCodes.InternalServerError, isError = true)
                  }
                case None =>
                  completeWithLog(Errors.invalidJson, StatusCodes.BadRequest)
              }
            }
        } ~ delete {
          log.info("[DELETE] /user")
          if (!checkAuth()) {
            completeWithLog(Errors.signedOut, StatusCodes.Unauthorized)
          }
          else
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
                      completeWithLog(failure, StatusCodes.InternalServerError, isError = true)
                  }
                case None =>
                  completeWithLog(Errors.invalidJson, StatusCodes.BadRequest)
              }
            }
        }
      }
    } ~ pathPrefix("users") {
      pathEnd {
        get {
          log.info("[GET] /users")
          if (!checkAuth()) {
            completeWithLog(Errors.signedOut, StatusCodes.Unauthorized)
          }
          else
            parameters('page.as[Int] ? 1, 'limit.as[Int] ? 10) { (page, limit) =>
              val result: Future[OutModels.GetUsers] =
                (userActor ? GetUsers(InModels.GetUsers(page, limit))).mapTo[OutModels.GetUsers]
              onComplete(result) {
                case Success(users) =>
                  completeWithLog(users, StatusCodes.OK)
                case Failure(failure) =>
                  completeWithLog(failure, StatusCodes.InternalServerError, isError = true)
              }
            }
        }
      }
    } ~ pathPrefix("session") {
      pathEnd {
        get {
          log.info("[GET] /session")
          if (checkAuth()) {
            completeWithLog(Messages.signedIn, StatusCodes.OK)
          }
          else {
            completeWithLog(Messages.signedOut, StatusCodes.Unauthorized)
          }
        } ~ post {
          log.info("[POST] /session")
          if (!checkAuth()) {
            completeWithLog(Errors.signedIn, StatusCodes.BadRequest)
          }
          else
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
                          completeWithLog(failure, StatusCodes.InternalServerError, isError = true)
                      }
                  }
                case None =>
                  completeWithLog(Errors.invalidJson, StatusCodes.BadRequest)
              }
            }
        } ~ delete {
          log.info("[DELETE] /session")
          if (!checkAuth()) {
            completeWithLog(Errors.signedOut, StatusCodes.Unauthorized)
          }
          else {
            resetJwt() {
              completeWithLog(Messages.signedOut, StatusCodes.OK)
            }
          }
        }
      }
    }
  
  // other dependencies that UserRoutes use
  def userActor: ActorRef
}
