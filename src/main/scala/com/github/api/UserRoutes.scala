package com.github.api

import akka.actor.{ActorRef, ActorSystem}
import akka.event.Logging
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.{delete, get, post}
import akka.http.scaladsl.server.directives.PathDirectives.path
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.pattern.ask
import akka.util.Timeout
import com.github.common.UserActor._

import scala.concurrent.Future
import scala.concurrent.duration._

trait UserRoutes extends JsonSupport {
    
    // we leave these abstract, since they will be provided by the App
    implicit def system: ActorSystem
    
    lazy val log = Logging(system, classOf[UserRoutes])
    lazy val userRoutes: Route =
        concat(
            pathPrefix("user") {
                concat(
                    pathEnd {
                        concat(
                            // TODO: This method gives away data of signed in user
                            get {
                                entity(as[String]) { _ =>
                                    rejectEmptyResponse {
                                        complete((StatusCodes.OK, "OK"))
                                    }
                                }
                            },
                            // TODO: This method creates records about new user and sends auth cookie to user
                            post {
                                entity(as[String]) { _ =>
                                    rejectEmptyResponse {
                                        complete((StatusCodes.OK, "OK"))
                                    }
                                    //val userCreated: Future[ActionPerformed] =
                                    //    (userRegistryActor ? CreateUser(user, "password")).mapTo[ActionPerformed]
                                    //onSuccess(userCreated) { performed =>
                                    //    log.info("Created user [{}]: {}", user.name, performed.message)
                                    //    complete((StatusCodes.Created, performed))
                                    //}
                                }
                            },
                            // TODO: This method updates info of signed in user
                            put {
                                entity(as[String]) { _ =>
                                    rejectEmptyResponse {
                                        complete((StatusCodes.OK, "OK"))
                                    }
                                }
                            },
                            // TODO: This method deletes all information about user
                            delete {
                                entity(as[String]) { _ =>
                                    rejectEmptyResponse {
                                        complete((StatusCodes.OK, "OK"))
                                    }
                                    //val userDeleted: Future[ActionPerformed] =
                                    //    (userRegistryActor ? DeleteUser(name)).mapTo[ActionPerformed]
                                    //onSuccess(userDeleted) { performed =>
                                    //    log.info("Deleted user [{}]: {}", name, performed.description)
                                    //    complete((StatusCodes.OK, performed))
                                    //}
                                }
                            },
                        )
                    },
                    path(Segment) { email =>
                        concat(
                            // TODO: This method gives away user data by required email
                            get {
                                //val maybeUser: Future[Option[User]] =
                                //    (userRegistryActor ? GetUser(email)).mapTo[Option[User]]
                                //rejectEmptyResponse {
                                //   complete(maybeUser)
                                //}
                                rejectEmptyResponse {
                                    complete((StatusCodes.OK, "OK"))
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
                            // TODO: This method gives away users data limited with {limit} entries per page
                            //  and offset of {page} from the first page
                            get {
                                //val users: Future[Users] =
                                //    (userRegistryActor ? GetUsers).mapTo[Users]
                                //complete(users)
                                rejectEmptyResponse {
                                    complete((StatusCodes.OK, "OK"))
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
                            // TODO: This method checks whether user is signed in or signed out
                            get {
                                entity(as[String]) { _ =>
                                    rejectEmptyResponse {
                                        complete((StatusCodes.OK, "OK"))
                                    }
                                }
                            },
                            // TODO: This method signes user in and sets cookie
                            post {
                                entity(as[String]) { _ =>
                                    rejectEmptyResponse {
                                        complete((StatusCodes.OK, "OK"))
                                    }
                                }
                            },
                            // TODO: This method signed user out and deletes cookie
                            delete {
                                entity(as[String]) { _ =>
                                    rejectEmptyResponse {
                                        complete((StatusCodes.OK, "OK"))
                                    }
                                }
                            },
                        )
                    },
                )
            },
            pathPrefix("room") {
                concat(
                    pathEnd {
                        concat(
                            // TODO: This method gives away data about room (maybe bookings)
                            get {
                                entity(as[String]) { _ =>
                                    rejectEmptyResponse {
                                        complete((StatusCodes.OK, "OK"))
                                    }
                                }
                            },
                            // TODO: This method creating new room
                            post {
                                entity(as[String]) { _ =>
                                    rejectEmptyResponse {
                                        complete((StatusCodes.OK, "OK"))
                                    }
                                }
                            },
                            // TODO: This method delete room by number
                            delete {
                                entity(as[String]) { _ =>
                                    rejectEmptyResponse {
                                        complete((StatusCodes.OK, "OK"))
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
                            // TODO: This method booking room
                            post {
                                entity(as[String]) { _ =>
                                    rejectEmptyResponse {
                                        complete((StatusCodes.OK, "OK"))
                                    }
                                }
                            },
                            // TODO: This method free room
                            delete {
                                entity(as[String]) { _ =>
                                    rejectEmptyResponse {
                                        complete((StatusCodes.OK, "OK"))
                                    }
                                }
                            },
                        )
                    },
                )
            },
        )
    
    // Required by the `ask` (?) method below
    implicit lazy val timeout: Timeout = Timeout(5.seconds) // usually we'd obtain the timeout from the system's configuration
    // other dependencies that UserRoutes use
    def userRegistryActor: ActorRef
}
