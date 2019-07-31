package com.github.api

import akka.actor.{ActorRef, ActorSystem}
import akka.event.Logging
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.{delete, get, post}
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.util.Timeout

import scala.concurrent.duration._

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
                            // TODO: This method gives away data about room (maybe bookings)
                            get {
                                roomLog.info("[GET] /room")
                                rejectEmptyResponse {
                                    complete((StatusCodes.OK, "OK"))
                                }
                            },
                            // TODO: This method creating new room
                            post {
                                roomLog.info("[POST] /room")
                                rejectEmptyResponse {
                                    complete((StatusCodes.OK, "OK"))
                                }
                            },
                            // TODO: This method delete room by number
                            delete {
                                roomLog.info("[DELETE] /room")
                                rejectEmptyResponse {
                                    complete((StatusCodes.OK, "OK"))
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
                            // TODO: This method booking room
                            post {
                                roomLog.info("[POST] /booking")
                                rejectEmptyResponse {
                                    complete((StatusCodes.OK, "OK"))
                                }
                            },
                            // TODO: This method free room
                            delete {
                                roomLog.info("[DELETE] /booking")
                                rejectEmptyResponse {
                                    complete((StatusCodes.OK, "OK"))
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
