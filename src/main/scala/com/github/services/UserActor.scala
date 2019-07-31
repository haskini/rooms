package com.github.services

import akka.actor.{Actor, ActorLogging, Props}
import com.github.common._

object UserActor {
    
    def props: Props = Props[UserActor]
    
    final case class ActionPerformed(message: String)
    
    final case class GetUser(data: InModels.GetUser)
    final case class CheckPassword(data: InModels.CheckPassword)
    final case class CreateUser(data: InModels.CreateUser)
    final case class UpdateUser(data: InModels.UpdateUser)
    final case class UpdatePassword(data: InModels.UpdatePassword)
    final case class DeleteUser(data: InModels.DeleteUser)
    
    final case class GetUsers(data: InModels.GetUsers)
    
}

class UserActor extends Actor with ActorLogging {
    import UserActor._
    
    def receive: Receive = {
        case GetUsers(data) =>
            sender() ! ActionPerformed(s"some users on page ${data.page} with limit ${data.limit}")
        // TODO: Add all case classes
    }
}
