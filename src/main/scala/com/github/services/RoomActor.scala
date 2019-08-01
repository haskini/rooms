package com.github.services

import akka.actor.{Actor, ActorLogging, Props}
import com.github.common._
import com.github.persistence.DbRoom

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
  
  def receive: Receive = {
    case GetRoom(input) =>
      DbRoom.GetRoom(input.number) match {
        case Right(room) => sender() ! room
        case Left(error) =>
          error match {
            case NotFound => sender() ! Errors.roomNotFound
            case DbError(msg) => sender() ! Errors.db(msg)
            case _ => sender() ! Errors.unknown
          }
      }
    case CreateRoom(input) =>
      DbRoom.GetRoom(input.number) match {
        case Right(_) =>
          sender() ! Errors.roomExists
        case Left(gettingError) =>
          gettingError match {
            case NotFound =>
              DbRoom.CreateRoom(DbModels.Room(
                number = input.number,
                bookings = List(),
              )) match {
                case None => sender() ! Messages.created
                case Some(error) =>
                  error match {
                    case DbError(msg) => sender() ! Errors.db(msg)
                    case _ => sender() ! Errors.unknown
                  }
              }
            case DbError(msg) => sender() ! Errors.db(msg)
            case _ => sender() ! Errors.unknown
          }
      }
    case BookRoom(jwt, input) =>
      DbRoom.GetRoom(input.number) match {
        case Right(room) =>
          // Intersecting ranges exists
          if (room.bookings.exists(booking => booking.start < input.stop && booking.stop > input.start))
            sender() ! Errors.roomBusy
          else
            DbRoom.BookRoom(room.number, DbModels.Booking(
              start = input.start,
              stop = input.stop,
              userEmail = jwt.email,
            )) match {
              case None => sender() ! Messages.updated
              case Some(error) =>
                error match {
                  case DbError(msg) => sender() ! Errors.db(msg)
                  case _ => sender() ! Errors.unknown
                }
            }
        case Left(error) =>
          error match {
            case NotFound => sender() ! Errors.roomNotFound
            case DbError(msg) => sender() ! Errors.db(msg)
            case _ => sender() ! Errors.unknown
          }
      }
    case FreeRoom(jwt, input) =>
      DbRoom.GetRoom(input.number) match {
        case Right(room) =>
          if (room.bookings.exists(_.start == input.start))
            room.bookings.filter(_.start == input.start).foreach(booking =>
              if (jwt.isAdmin || booking.userEmail == jwt.email)
                DbRoom.FreeRoom(room.number, booking.start) match {
                  case None => sender() ! Messages.updated
                  case Some(error) =>
                    error match {
                      case DbError(msg) => sender() ! Errors.db(msg)
                      case _ => sender() ! Errors.unknown
                    }
                }
              else
                sender() ! Errors.noPermissions
            )
          else
            sender() ! Errors.bookingNotFound
        case Left(error) =>
          error match {
            case NotFound => sender() ! Errors.roomNotFound
            case DbError(msg) => sender() ! Errors.db(msg)
            case _ => sender() ! Errors.unknown
          }
      }
    case DeleteRoom(input) =>
      DbRoom.GetRoom(input.number) match {
        case Right(room) =>
          DbRoom.DeleteRoom(room.number) match {
            case None => sender() ! Messages.deleted
            case Some(error) =>
              error match {
                case DbError(msg) => sender() ! Errors.db(msg)
                case _ => sender() ! Errors.unknown
              }
          }
        case Left(error) =>
          error match {
            case NotFound => sender() ! Errors.roomNotFound
            case DbError(msg) => sender() ! Errors.db(msg)
            case _ => sender() ! Errors.unknown
          }
      }
    case GetRooms(input) =>
      DbRoom.GetRooms((input.page - 1) * input.limit, input.limit) match {
        case Right(rooms) => sender() ! rooms
        case Left(error) =>
          error match {
            case DbError(msg) => sender() ! Errors.db(msg)
            case _ => sender() ! Errors.unknown
          }
      }
  }
}
