package com.github.services

import akka.actor.{Actor, ActorLogging, Props}
import com.github.common._

object RoomActor {
    
    def props: Props = Props[RoomActor]
    
    final case class ActionPerformed(message: String)
    
    final case class GetRoom(data: InModels.GetRoom)
    final case class CreateRoom(data: InModels.CreateRoom)
    final case class DeleteRoom(data: InModels.DeleteRoom)
    
    final case class GetRooms(data: InModels.GetRooms)

    final case class BookRoom(data: InModels.BookRoom)
    final case class FreeRoom(data: InModels.FreeRoom)
    
}

class RoomActor extends Actor with ActorLogging {
    import RoomActor._
    
    def receive: Receive = {
        case GetRooms(data) =>
            sender() ! ActionPerformed(s"some rooms on page ${data.page} with limit ${data.limit}")
        // TODO: Add all case classes
    }
}
