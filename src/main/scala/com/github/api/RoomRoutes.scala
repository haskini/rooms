package com.github.api

import akka.actor.{ActorRef, ActorSystem}
import akka.event.Logging
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.{delete, get, post}
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.pattern.ask
import akka.util.Timeout
import com.github.common._
import com.github.services.RoomActor._
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization.write

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}

trait RoomRoutes {
  this: Routing =>
  
  lazy val roomRoutes: Route =
    concat(
      pathPrefix("room") {
        concat(
          pathEnd {
            concat(
              get {
                log.info("[GET] /room")
                if (false) {
                  val outJson = write(Errors.signedOut)
                  log.debug(outJson)
                  complete((StatusCodes.Unauthorized, outJson))
                }
                else
                  parameter('number.?) {
                    case Some(number) =>
                      val answer = roomActor ? GetRoom(InModels.GetRoom(number))
                      val roomFuture: Future[OutModels.GetRoom] = answer.mapTo[OutModels.GetRoom]
                      onComplete(roomFuture) {
                        case Success(room) =>
                          val outJson = write(room)
                          log.debug(outJson)
                          complete((StatusCodes.OK, outJson))
                        case Failure(_) =>
                          val messageFuture: Future[OutModels.MessageWithCode] =
                            answer.mapTo[OutModels.MessageWithCode]
                          onComplete(messageFuture) {
                            case Success(room) =>
                              val outJson = write(room)
                              log.debug(outJson)
                              complete((StatusCodes.OK, outJson))
                            case Failure(failure) =>
                              val outJson = write(failure)
                              log.error(outJson)
                              complete((StatusCodes.InternalServerError, outJson))
                          }
                      }
                    case None => complete((StatusCodes.BadRequest, "Incorrect number!"))
                  }
              },
              post {
                log.info("[POST] /room")
                if (false) {
                  val outJson = write(Errors.signedOut)
                  log.debug(outJson)
                  complete((StatusCodes.Unauthorized, outJson))
                }
                else
                  entity(as[String]) { data =>
                    parse(data).extractOpt[InModels.CreateRoom] match {
                      case Some(input) =>
                        val result: Future[OutModels.MessageWithCode] =
                          (roomActor ? CreateRoom(input)).mapTo[OutModels.MessageWithCode]
                        onComplete(result) {
                          case Success(msg) =>
                            msg match {
                              case OutModels.Error(_, _) =>
                                val outJson = write(msg)
                                log.debug(outJson)
                                complete((StatusCodes.BadRequest, outJson))
                              case OutModels.Message(_, _) =>
                                val outJson = write(msg)
                                log.debug(outJson)
                                complete((StatusCodes.Created, outJson))
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
                log.info("[DELETE] /room")
                if (false) {
                  val outJson = write(Errors.signedOut)
                  log.debug(outJson)
                  complete((StatusCodes.Unauthorized, outJson))
                }
                else
                  entity(as[String]) { data =>
                    parse(data).extractOpt[InModels.DeleteRoom] match {
                      case Some(input) =>
                        val result: Future[OutModels.MessageWithCode] =
                          (roomActor ? DeleteRoom(input)).mapTo[OutModels.MessageWithCode]
                        onComplete(result) {
                          case Success(msg) =>
                            msg match {
                              case OutModels.Error(_, _) =>
                                val outJson = write(msg)
                                log.debug(outJson)
                                complete((StatusCodes.BadRequest, outJson))
                              case OutModels.Message(_, _) =>
                                val outJson = write(msg)
                                log.debug(outJson)
                                complete((StatusCodes.OK, outJson))
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
      pathPrefix("booking") {
        concat(
          pathEnd {
            concat(
              post {
                log.info("[POST] /booking")
                if (false) {
                  val outJson = write(Errors.signedOut)
                  log.debug(outJson)
                  complete((StatusCodes.Unauthorized, outJson))
                }
                else
                  entity(as[String]) { data =>
                    parse(data).extractOpt[InModels.BookRoom] match {
                      case Some(input) =>
                        val result: Future[OutModels.MessageWithCode] =
                          (roomActor ? BookRoom(jwt, input)).mapTo[OutModels.MessageWithCode]
                        onComplete(result) {
                          case Success(msg) =>
                            msg match {
                              case OutModels.Error(_, _) =>
                                val outJson = write(msg)
                                log.debug(outJson)
                                complete((StatusCodes.BadRequest, outJson))
                              case OutModels.Message(_, _) =>
                                val outJson = write(msg)
                                log.debug(outJson)
                                complete((StatusCodes.OK, outJson))
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
                log.info("[DELETE] /booking")
                if (false) {
                  val outJson = write(Errors.signedOut)
                  log.debug(outJson)
                  complete((StatusCodes.Unauthorized, outJson))
                }
                else
                  entity(as[String]) { data =>
                    parse(data).extractOpt[InModels.FreeRoom] match {
                      case Some(input) =>
                        val result: Future[OutModels.MessageWithCode] =
                          (roomActor ? FreeRoom(jwt, input)).mapTo[OutModels.MessageWithCode]
                        onComplete(result) {
                          case Success(msg) =>
                            msg match {
                              case OutModels.Error(_, _) =>
                                val outJson = write(msg)
                                log.debug(outJson)
                                complete((StatusCodes.BadRequest, outJson))
                              case OutModels.Message(_, _) =>
                                val outJson = write(msg)
                                log.debug(outJson)
                                complete((StatusCodes.OK, outJson))
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
      pathPrefix("rooms") {
        concat(
          pathEnd {
            concat(
              get {
                log.info("[GET] /rooms")
                if (false) {
                  val outJson = write(Errors.signedOut)
                  log.debug(outJson)
                  complete((StatusCodes.Unauthorized, outJson))
                }
                else
                  parameters('page.as[Int] ? 1, 'limit.as[Int] ? 10) { (page, limit) =>
                    val result: Future[OutModels.GetRooms] =
                      (roomActor ? GetRooms(InModels.GetRooms(page, limit))).mapTo[OutModels.GetRooms]
                    onComplete(result) {
                      case Success(rooms) =>
                        val outJson = write(rooms)
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
    )
  
  // other dependencies that RoomRoutes use
  def roomActor: ActorRef
}
