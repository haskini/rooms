package com.github

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.github.api.UserRoutes
import com.github.common.UserActor

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

object ApiServer extends App with UserRoutes {
    
    // set up ActorSystem and other dependencies here
    implicit val system: ActorSystem = ActorSystem("helloAkkaHttpServer")
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    implicit val executionContext: ExecutionContext = system.dispatcher
    // from the UserRoutes trait
    lazy val routes: Route = userRoutes
    val userRegistryActor: ActorRef = system.actorOf(UserActor.props, "userRegistryActor")
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
