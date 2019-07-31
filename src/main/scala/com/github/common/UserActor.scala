package com.github.common

import akka.actor.{Actor, ActorLogging, Props}

final case class User(email: String, name: String, isAdmin: String)

final case class Users(users: Seq[User])

object UserActor {
    
    def props: Props = Props[UserActor]
    
    final case class ActionPerformed(message: String)
    
    final case class GetUser(email: String)
    
    final case class CheckPassword(email: String, password: String)
    
    final case class CreateUser(user: User, password: String)
    
    final case class UpdateUser(email: String, user: User)
    
    final case class UpdatePassword(email: String, oldPassword: String, newPassword: String)
    
    final case class DeleteUser(email: String, password: String)
    
    final case object GetUsers
    
}

class UserActor extends Actor with ActorLogging {
    import UserActor._
    
    final case class DbUser(user: User, password: String)
    
    var users = Set.empty[DbUser]
    
    def receive: Receive = {
        case GetUsers =>
            sender() ! Users(users.map(_.user).toSeq)
        case CreateUser(user, password) =>
            users += DbUser(user, password)
            sender() ! ActionPerformed(s"User ${user.email} created.")
        case GetUser(email) =>
            sender() ! users.find(_.user.email == email)
        case DeleteUser(email, password) =>
            users.find(_.user.email == email) foreach { user =>
                if (user.password == password) {
                    users -= user
                    sender() ! ActionPerformed(s"User $email deleted.")
                } else {
                    sender() ! ActionPerformed(s"Wrong password!.")
                }
            }
        // TODO: Add all case classes
    }
}
