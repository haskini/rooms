package com.github.api

import akka.actor.ActorRef
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.{delete, get, post}
import akka.pattern.ask
import com.github.common._
import com.github.services.RoomActor._
import org.json4s._
import org.json4s.native.JsonMethods._

import scala.concurrent.Future
import scala.util.{Failure, Success}

trait RoomRoutes {
  this: Routing =>
  
  lazy val roomRoutes: Route =
    pathPrefix("room") {
      pathEndOrSingleSlash {
        (get & checkAuth) { maybeJwt =>
          log.info("[GET] /room")
          maybeJwt match {
            case None =>
              deleteCookie(jwtCookieName) {
                completeWithLog(Errors.signedOut, StatusCodes.Unauthorized)
              }
            case Some(_) =>
              parameter('number.?) {
                case Some(number) =>
                  val answer = roomActor ? GetRoom(InModels.GetRoom(number))
                  val roomFuture: Future[OutModels.GetRoom] = answer.mapTo[OutModels.GetRoom]
                  onComplete(roomFuture) {
                    case Success(room) =>
                      completeWithLog(room, StatusCodes.OK)
                    case Failure(_) =>
                      val messageFuture: Future[OutModels.MessageWithCode] =
                        answer.mapTo[OutModels.MessageWithCode]
                      onComplete(messageFuture) {
                        case Success(room) =>
                          completeWithLog(room, StatusCodes.OK)
                        case Failure(failure) =>
                          completeWithLog(failure.toString, StatusCodes.InternalServerError, isError = true)
                      }
                  }
                case None =>
                  completeWithLog(Errors.invalidNumber, StatusCodes.BadRequest)
              }
          }
        } ~ (post & checkAuth) { maybeJwt =>
          log.info("[POST] /room")
          maybeJwt match {
            case None =>
              deleteCookie(jwtCookieName) {
                completeWithLog(Errors.signedOut, StatusCodes.Unauthorized)
              }
            case Some(_) =>
              entity(as[String]) { data =>
                parse(data).extractOpt[InModels.CreateRoom] match {
                  case Some(input) =>
                    val result: Future[OutModels.MessageWithCode] =
                      (roomActor ? CreateRoom(input)).mapTo[OutModels.MessageWithCode]
                    onComplete(result) {
                      case Success(msg) =>
                        msg match {
                          case OutModels.Error(_, _) =>
                            completeWithLog(msg, StatusCodes.BadRequest)
                          case OutModels.Message(_, _) =>
                            completeWithLog(msg, StatusCodes.Created)
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
          log.info("[DELETE] /room")
          maybeJwt match {
            case None =>
              deleteCookie(jwtCookieName) {
                completeWithLog(Errors.signedOut, StatusCodes.Unauthorized)
              }
            case Some(_) =>
              entity(as[String]) { data =>
                parse(data).extractOpt[InModels.DeleteRoom] match {
                  case Some(input) =>
                    val result: Future[OutModels.MessageWithCode] =
                      (roomActor ? DeleteRoom(input)).mapTo[OutModels.MessageWithCode]
                    onComplete(result) {
                      case Success(msg) =>
                        msg match {
                          case OutModels.Error(_, _) =>
                            completeWithLog(msg, StatusCodes.BadRequest)
                          case OutModels.Message(_, _) =>
                            completeWithLog(msg, StatusCodes.OK)
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
    } ~ pathPrefix("booking") {
      pathEndOrSingleSlash {
        (post & checkAuth) { maybeJwt =>
          log.info("[POST] /booking")
          maybeJwt match {
            case None =>
              deleteCookie(jwtCookieName) {
                completeWithLog(Errors.signedOut, StatusCodes.Unauthorized)
              }
            case Some(jwt) =>
              entity(as[String]) { data =>
                parse(data).extractOpt[InModels.BookRoom] match {
                  case Some(input) =>
                    val result: Future[OutModels.MessageWithCode] =
                      (roomActor ? BookRoom(jwt, input)).mapTo[OutModels.MessageWithCode]
                    onComplete(result) {
                      case Success(msg) =>
                        msg match {
                          case OutModels.Error(_, _) =>
                            completeWithLog(msg, StatusCodes.BadRequest)
                          case OutModels.Message(_, _) =>
                            completeWithLog(msg, StatusCodes.OK)
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
          log.info("[DELETE] /booking")
          maybeJwt match {
            case None =>
              deleteCookie(jwtCookieName) {
                completeWithLog(Errors.signedOut, StatusCodes.Unauthorized)
              }
            case Some(jwt) =>
              entity(as[String]) { data =>
                parse(data).extractOpt[InModels.FreeRoom] match {
                  case Some(input) =>
                    val result: Future[OutModels.MessageWithCode] =
                      (roomActor ? FreeRoom(jwt, input)).mapTo[OutModels.MessageWithCode]
                    onComplete(result) {
                      case Success(msg) =>
                        msg match {
                          case OutModels.Error(_, _) =>
                            completeWithLog(msg, StatusCodes.BadRequest)
                          case OutModels.Message(_, _) =>
                            completeWithLog(msg, StatusCodes.OK)
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
    } ~ pathPrefix("rooms") {
      pathEndOrSingleSlash {
        (get & checkAuth) { maybeJwt =>
          log.info("[GET] /rooms")
          maybeJwt match {
            case None =>
              deleteCookie(jwtCookieName) {
                completeWithLog(Errors.signedOut, StatusCodes.Unauthorized)
              }
            case Some(_) =>
              parameters('page.as[Int] ? 1, 'limit.as[Int] ? 10) { (page, limit) =>
                val result: Future[OutModels.GetRooms] =
                  (roomActor ? GetRooms(InModels.GetRooms(page, limit))).mapTo[OutModels.GetRooms]
                onComplete(result) {
                  case Success(rooms) =>
                    completeWithLog(rooms, StatusCodes.OK)
                  case Failure(failure) =>
                    completeWithLog(failure.toString, StatusCodes.InternalServerError, isError = true)
                }
              }
          }
        }
      }
    }
  
  // other dependencies that RoomRoutes use
  def roomActor: ActorRef
}
