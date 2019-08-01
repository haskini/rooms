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
  
  // we leave these abstract, since they will be provided by the App
  implicit def system: ActorSystem
  
  lazy val roomLog = Logging(system, classOf[RoomRoutes])
  lazy val roomRoutes: Route =
    concat(
      pathPrefix("room") {
        concat(
          pathEnd {
            concat(
              get {
                roomLog.info("[GET] /room")
                if (false) {
                  val outJson = write(Errors.signedOut)
                  roomLog.debug(outJson)
                  complete((StatusCodes.Unauthorized, outJson))
                }
                else
                  entity(as[String]) { data =>
                    parse(data).extractOpt[InModels.GetRoom] match {
                      case Some(input) =>
                        val answer = roomActor ? GetRoom(input)
                        val roomFuture: Future[OutModels.GetRoom] = answer.mapTo[OutModels.GetRoom]
                        onComplete(roomFuture) {
                          case Success(room) =>
                            val outJson = write(room)
                            roomLog.debug(outJson)
                            complete((StatusCodes.OK, outJson))
                          case Failure(_) =>
                            val messageFuture: Future[OutModels.MessageWithCode] =
                              answer.mapTo[OutModels.MessageWithCode]
                            onComplete(messageFuture) {
                              case Success(room) =>
                                val outJson = write(room)
                                roomLog.debug(outJson)
                                complete((StatusCodes.OK, outJson))
                              case Failure(failure) =>
                                val outJson = write(failure)
                                roomLog.error(outJson)
                                complete((StatusCodes.InternalServerError, outJson))
                            }
                        }
                      case None => complete((StatusCodes.BadRequest, "Incorrect json!"))
                    }
                  }
              },
              post {
                roomLog.info("[POST] /room")
                if (false) {
                  val outJson = write(Errors.signedOut)
                  roomLog.debug(outJson)
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
                                roomLog.debug(outJson)
                                complete((StatusCodes.BadRequest, outJson))
                              case OutModels.Message(_, _) =>
                                val outJson = write(msg)
                                roomLog.debug(outJson)
                                complete((StatusCodes.Created, outJson))
                            }
                          case Failure(failure) =>
                            val outJson = write(failure)
                            roomLog.error(outJson)
                            complete((StatusCodes.InternalServerError, outJson))
                        }
                      case None => complete((StatusCodes.BadRequest, "Incorrect json!"))
                    }
                  }
              },
              delete {
                roomLog.info("[DELETE] /room")
                if (false) {
                  val outJson = write(Errors.signedOut)
                  roomLog.debug(outJson)
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
                                roomLog.debug(outJson)
                                complete((StatusCodes.BadRequest, outJson))
                              case OutModels.Message(_, _) =>
                                val outJson = write(msg)
                                roomLog.debug(outJson)
                                complete((StatusCodes.OK, outJson))
                            }
                          case Failure(failure) =>
                            val outJson = write(failure)
                            roomLog.error(outJson)
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
                roomLog.info("[POST] /booking")
                if (false) {
                  val outJson = write(Errors.signedOut)
                  roomLog.debug(outJson)
                  complete((StatusCodes.Unauthorized, outJson))
                }
                else
                  entity(as[String]) { data =>
                    parse(data).extractOpt[InModels.BookRoom] match {
                      case Some(input) =>
                        val result: Future[OutModels.MessageWithCode] =
                          (roomActor ? BookRoom(input)).mapTo[OutModels.MessageWithCode]
                        onComplete(result) {
                          case Success(msg) =>
                            msg match {
                              case OutModels.Error(_, _) =>
                                val outJson = write(msg)
                                roomLog.debug(outJson)
                                complete((StatusCodes.BadRequest, outJson))
                              case OutModels.Message(_, _) =>
                                val outJson = write(msg)
                                roomLog.debug(outJson)
                                complete((StatusCodes.OK, outJson))
                            }
                          case Failure(failure) =>
                            val outJson = write(failure)
                            roomLog.error(outJson)
                            complete((StatusCodes.InternalServerError, outJson))
                        }
                      case None => complete((StatusCodes.BadRequest, "Incorrect json!"))
                    }
                  }
              },
              delete {
                roomLog.info("[DELETE] /booking")
                if (false) {
                  val outJson = write(Errors.signedOut)
                  roomLog.debug(outJson)
                  complete((StatusCodes.Unauthorized, outJson))
                }
                else
                  entity(as[String]) { data =>
                    parse(data).extractOpt[InModels.FreeRoom] match {
                      case Some(input) =>
                        val result: Future[OutModels.MessageWithCode] =
                          (roomActor ? FreeRoom(input)).mapTo[OutModels.MessageWithCode]
                        onComplete(result) {
                          case Success(msg) =>
                            msg match {
                              case OutModels.Error(_, _) =>
                                val outJson = write(msg)
                                roomLog.debug(outJson)
                                complete((StatusCodes.BadRequest, outJson))
                              case OutModels.Message(_, _) =>
                                val outJson = write(msg)
                                roomLog.debug(outJson)
                                complete((StatusCodes.OK, outJson))
                            }
                          case Failure(failure) =>
                            val outJson = write(failure)
                            roomLog.error(outJson)
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
    )
  
  implicit val formats: DefaultFormats.type = DefaultFormats
  // Required by the `ask` (?) method below
  implicit lazy val timeout: Timeout = Timeout(5.seconds) // usually we'd obtain the timeout from the system's configuration
  // other dependencies that RoomRoutes use
  def roomActor: ActorRef
}
