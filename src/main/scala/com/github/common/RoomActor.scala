package com.github.common

import akka.actor.{Actor, ActorLogging, Props}
import akka.http.scaladsl.model.DateTime

final case class Booking(time: DateTime, owner: String)

final case class Room(number: String, bookings: Seq[Booking])

final case class Rooms(rooms: Seq[Room])

object RoomActor {
    
    def props: Props = Props[RoomActor]
    
    final case class ActionPerformed(message: String)
    
    final case class GetRoom(number: String)
    
    final case class CreateRoom(room: Room)
    
    final case class BookRoom(number: String, booking: Booking)
    
    final case class FreeRoom(number: String, time: DateTime)
    
    final case class DeleteRoom(number: String)
    
    final case object GetRooms
    
}

class RoomActor extends Actor with ActorLogging {
    import RoomActor._
    
    var rooms = Set.empty[Room]
    
    def receive: Receive = {
        case GetRooms =>
            sender() ! Rooms(rooms.toSeq)
        // TODO: Add all case classes
    }
}
