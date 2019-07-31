package com.github.api

import akka.actor.{ActorRef, ActorSystem}
import akka.event.{Logging, LoggingBus}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.{delete, get, post}
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.util.Timeout

import scala.concurrent.duration._

trait UserRoutes {
    
    // we leave these abstract, since they will be provided by the App
    implicit def system: ActorSystem
    
    lazy val userLog = Logging(system, classOf[UserRoutes])
    lazy val userRoutes: Route =
        concat(
            pathPrefix("user") {
                concat(
                    pathEnd {
                        concat(
                            // TODO: This method gives away data of signed in user
                            get {
                                //entity(as[String]) { _ =>
                                userLog.info("[GET] /user")
                                rejectEmptyResponse {
                                    complete((StatusCodes.OK, "I don't know about you!"))
                                }
                                //}
                            },
                            // TODO: This method creates records about new user and sends auth cookie to user
                            post {
                                userLog.info("[POST] /user")
                                rejectEmptyResponse {
                                    complete((StatusCodes.OK, "OK"))
                                }
                                //val userCreated: Future[ActionPerformed] =
                                //    (userActor ? CreateUser(user, "password")).mapTo[ActionPerformed]
                                //onSuccess(userCreated) { performed =>
                                //    log.info("Created user [{}]: {}", user.name, performed.message)
                                //    complete((StatusCodes.Created, performed))
                                //}
                                
                            },
                            // TODO: This method updates info of signed in user
                            put {
                                userLog.info("[PUT] /user")
                                rejectEmptyResponse {
                                    complete((StatusCodes.OK, "OK"))
                                }
                            },
                            // TODO: This method deletes all information about user
                            delete {
                                userLog.info("[DELETE] /user")
                                rejectEmptyResponse {
                                    complete((StatusCodes.OK, "OK"))
                                }
                                //val userDeleted: Future[ActionPerformed] =
                                //    (userActor ? DeleteUser(name)).mapTo[ActionPerformed]
                                //onSuccess(userDeleted) { performed =>
                                //    log.info("Deleted user [{}]: {}", name, performed.description)
                                //    complete((StatusCodes.OK, performed))
                                //}
                                
                            },
                        )
                    },
                )
            },
            pathPrefix("users") {
                concat(
                    pathEnd {
                        concat(
                            // TODO: This method gives away users data limited with {limit} entries per page
                            //  and offset of {page} from the first page
                            get {
                                userLog.info("[GET] /users")
                                rejectEmptyResponse {
                                    complete((StatusCodes.OK, "OK"))
                                }
                                //val users: Future[Users] =
                                //    (userActor ? GetUsers).mapTo[Users]
                                //complete(users)
                            },
                        )
                    },
                )
            },
            pathPrefix("session") {
                concat(
                    pathEnd {
                        concat(
                            // TODO: This method checks whether user is signed in or signed out
                            get {
                                userLog.info("[GET] /session")
                                rejectEmptyResponse {
                                    complete((StatusCodes.OK, "OK"))
                                }
                            },
                            // TODO: This method signes user in and sets cookie
                            post {
                                userLog.info("[POST] /post")
                                rejectEmptyResponse {
                                    complete((StatusCodes.OK, "OK"))
                                }
                            },
                            // TODO: This method signed user out and deletes cookie
                            delete {
                                userLog.info("[DELETE] /delete")
                                rejectEmptyResponse {
                                    complete((StatusCodes.OK, "OK"))
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
