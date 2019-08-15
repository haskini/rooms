package com.github.services

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.github.common._
import com.github.persistence.DbRoom

import scala.concurrent.ExecutionContext.Implicits.global

object RoomActor {
  def props: Props = Props[RoomActor]
  
  final case class GetRoom(data: InModels.GetRoom)
  final case class CreateRoom(data: InModels.CreateRoom)
  final case class DeleteRoom(data: InModels.DeleteRoom)
  
  final case class GetRooms(data: InModels.GetRooms)
  
  final case class BookRoom(jwt: JwtModel, data: InModels.BookRoom)
  final case class FreeRoom(jwt: JwtModel, data: InModels.FreeRoom)
}

class RoomActor extends Actor with ActorLogging {
  import RoomActor._
  
  private def ErrorHandler(
    s: ActorRef,
    error: ErrorType,
    notFound: Option[ActorRef => Unit] = None,
    alreadyExists: Option[ActorRef => Unit] = None,
  ): Unit = {
    error match {
      case AlreadyExist => alreadyExists match {
        case Some(f) => f(s)
        case None => s ! Errors.roomExists
      }
      case NotFound => notFound match {
        case Some(f) => f(s)
        case None => s ! Errors.roomNotFound
      }
      case DbError(msg) => s ! Errors.db(msg)
      case _ => s ! Errors.unknown()
    }
  }
  
  private def DbToApiRoom(data: DbModels.Room): OutModels.GetRoom = {
    OutModels.GetRoom(
      number = data.number,
      bookings = data.bookings.map(booking => OutModels.Booking(
        start = booking.start,
        stop = booking.stop,
        userEmail = booking.userEmail,
      ))
    )
  }
  
  private def DbToApiRooms(data: Set[DbModels.Room]): OutModels.GetRooms = {
    OutModels.GetRooms(data.map(room => DbToApiRoom(room)))
  }
  
  private def GetRoomHandler(input: InModels.GetRoom): Unit = {
    val s = sender
    DbRoom.GetRoom(input.number) map {
      case Right(room) => s ! DbToApiRoom(room)
      case Left(error) => ErrorHandler(s, error)
    }
  }
  
  private def CreateRoomHandler(input: InModels.CreateRoom): Unit = {
    val s = sender
    DbRoom.GetRoom(input.number) map {
      case Right(_) => s ! Errors.roomExists
      case Left(error) => ErrorHandler(s, error, notFound = Option(s => {
        DbRoom.CreateRoom(DbModels.Room(
          number = input.number,
          bookings = Set(),
        )) map {
          case None => s ! Messages.created
          case Some(error) => ErrorHandler(s, error)
        }
      }))
    }
  }
  
  private def DeleteRoomHandler(input: InModels.DeleteRoom): Unit = {
    val s = sender
    DbRoom.GetRoom(input.number) map {
      case Right(room) =>
        DbRoom.DeleteRoom(room.number) map {
          case None => s ! Messages.deleted
          case Some(error) => ErrorHandler(s, error)
        }
      case Left(error) => ErrorHandler(s, error)
    }
  }
  
  private def GetRoomsHandler(input: InModels.GetRooms): Unit = {
    val s = sender
    DbRoom.GetRooms((input.page - 1) * input.limit, input.limit) map {
      case Right(rooms) => s ! DbToApiRooms(rooms)
      case Left(error) => ErrorHandler(s, error)
    }
  }
  
  private def BookRoomHandler(jwt: JwtModel, input: InModels.BookRoom): Unit = {
    val s = sender
    DbRoom.GetRoom(input.number) map {
      case Right(room) =>
        // Intersecting ranges exists
        if (room.bookings.exists(booking => booking.start < input.stop && booking.stop > input.start))
          s ! Errors.roomBusy
        else
          DbRoom.BookRoom(room.number, DbModels.Booking(
            start = input.start,
            stop = input.stop,
            userEmail = jwt.email,
          )) map {
            case None => s ! Messages.updated
            case Some(error) => ErrorHandler(s, error)
          }
      case Left(error) => ErrorHandler(s, error)
    }
  }
  
  private def FreeRoomHandler(jwt: JwtModel, input: InModels.FreeRoom): Unit = {
    val s = sender
    DbRoom.GetRoom(input.number) map {
      case Right(room) =>
        if (room.bookings.exists(_.start == input.start))
          room.bookings.filter(_.start == input.start).foreach(booking =>
            if (jwt.isAdmin || booking.userEmail == jwt.email)
              DbRoom.FreeRoom(room.number, booking.start) map {
                case None => s ! Messages.updated
                case Some(error) => ErrorHandler(s, error)
              }
            else
              s ! Errors.noPermissions
          )
        else
          s ! Errors.bookingNotFound
      case Left(error) => ErrorHandler(s, error)
    }
  }
  
  def receive: Receive = {
    case GetRoom(input) => GetRoomHandler(input)
    case CreateRoom(input) => CreateRoomHandler(input)
    case BookRoom(jwt, input) => BookRoomHandler(jwt, input)
    case FreeRoom(jwt, input) => FreeRoomHandler(jwt, input)
    case DeleteRoom(input) => DeleteRoomHandler(input)
    case GetRooms(input) => GetRoomsHandler(input)
  }
}
