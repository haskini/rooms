package com.github

import akka.actor.{Actor, ActorLogging, Props}

final case class User(name: String, age: Int, countryOfResidence: String)

final case class Users(users: Seq[User])

object UserRegistryActor {
    
    def props: Props = Props[UserRegistryActor]
    
    final case class ActionPerformed(description: String)
    
    final case class CreateUser(user: User)
    
    final case class GetUser(name: String)
    
    final case class DeleteUser(name: String)
    
    final case object GetUsers
}

class UserRegistryActor extends Actor with ActorLogging {
    import UserRegistryActor._
    
    var users = Set.empty[User]
    
    def receive: Receive = {
        case GetUsers =>
            sender() ! Users(users.toSeq)
        case CreateUser(user) =>
            users += user
            sender() ! ActionPerformed(s"User ${user.name} created.")
        case GetUser(name) =>
            sender() ! users.find(_.name == name)
        case DeleteUser(name) =>
            users.find(_.name == name) foreach { user => users -= user }
            sender() ! ActionPerformed(s"User ${name} deleted.")
    }
}
