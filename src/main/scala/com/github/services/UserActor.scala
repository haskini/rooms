package com.github.services

import akka.actor.{Actor, ActorLogging, Props}
import com.github.common._

object UserActor {
    
    def props: Props = Props[UserActor]
    
    final case class ActionPerformed(message: String)
    
    final case class GetUser(data: GetUserIn)
    
    final case class CheckPassword(data: CheckPasswordIn)
    
    final case class CreateUser(data: CreateUserIn)
    
    final case class UpdateUser(data: UpdateUserIn)
    
    final case class UpdatePassword(data: UpdatePasswordIn)
    
    final case class DeleteUser(data: DeleteUserIn)
    
    final case class GetUsers(data: GetUsersIn)
    
}

class UserActor extends Actor with ActorLogging {
    import UserActor._
    import com.github.persistence.DbUser
    
    def receive: Receive = {
        case GetUser(data) =>
            sender() ! DbUser.GetUser(data)
        case CheckPassword(data) =>
            sender() ! DbUser.CheckPassword(data)
        case CreateUser(user) =>
            sender() ! DbUser.CreateUser(CreateUserIn(
                email = user.email,
                name = user.name,
                isAdmin = user.isAdmin,
                password = Helpers.HashPassword(user.password)
            ))
        case UpdateUser(data) =>
            sender() ! DbUser.UpdateUser(data)
        case UpdatePassword(data) =>
            sender() ! DbUser.UpdatePassword(data)
        case DeleteUser(data) =>
            sender() ! DbUser.DeleteUser(data)
        case GetUsers(data) =>
            sender() ! DbUser.GetUsers()
    }
}
