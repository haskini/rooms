package com.github.common

import akka.actor.{Actor, ActorLogging, Props}

object UserActor {
    
    def props: Props = Props[UserActor]
    
    final case class ActionPerformed(message: String)
    
    final case class GetUser(data: GetUserIn)
    
    final case class CheckPassword(data: CheckPasswordIn)
    
    final case class CreateUser(data: CreateUserIn)
    
    final case class UpdateUser(data: UpdateUserIn)
    
    final case class UpdatePassword(data: UpdatePasswordIn)
    
    final case class DeleteUser(data: DeleteUserIn)
    
    final case object GetUsers
    
}

class UserActor extends Actor with ActorLogging {
    import UserActor._
    import com.github.persistence.DbUser
    
    def receive: Receive = {
        case GetUsers =>
            sender() ! DbUser.GetUsers()
        // TODO: Add all case classes
    }
}
