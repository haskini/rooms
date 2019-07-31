package com.github

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import com.github.api._
import com.github.services._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

object ApiServer extends App with UserRoutes with RoomRoutes {
    
    // set up ActorSystem and other dependencies here
    implicit val system: ActorSystem = ActorSystem("roomsApi")
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    implicit val executionContext: ExecutionContext = system.dispatcher
    
    val userActor: ActorRef = system.actorOf(UserActor.props, "userActor")
    val roomActor: ActorRef = system.actorOf(RoomActor.props, "roomActor")
    
    // from the UserRoutes trait
    lazy val routes: Route = concat(
        userRoutes,
        roomRoutes,
    )
    val serverBinding: Future[Http.ServerBinding] = Http().bindAndHandle(routes, "localhost", 4567)
    
    serverBinding.onComplete {
        case Success(bound) =>
            println(s"Server online at http://${bound.localAddress.getHostString}:${bound.localAddress.getPort}/")
        case Failure(e) =>
            Console.err.println(s"Server could not start!")
            e.printStackTrace()
            system.terminate()
    }
    
    Await.result(system.whenTerminated, Duration.Inf)
}
