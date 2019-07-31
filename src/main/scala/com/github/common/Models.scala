package com.github.common

import akka.http.scaladsl.model.DateTime

// COMMON MODELS
final case class User(email: String, password: String, name: String, isAdmin: Boolean)

final case class Booking(time: DateTime, owner: String)
final case class Room(number: String, bookings: Seq[Booking])
final case class Rooms(rooms: Seq[Room])

// IN MODELS

final case class GetUserIn(email: String)
final case class GetUsersIn(page: Int, limit: Int)
final case class CheckPasswordIn(email: String, password: String)
final case class CreateUserIn(user: User)
final case class UpdateUserIn(oldEmail: String, email: String, name: String, isAdmin: Boolean)
final case class UpdatePasswordIn(email: String, oldPassword: String, newPassword: String)
final case class DeleteUserIn(email: String, password: String)

final case class GetRoomIn(number: Int)
final case class CreateRoomIn(room: Room)
final case class BookRoomIn(number: String, booking: Booking)
final case class FreeRoomIn(number: String, time: DateTime)
final case class DeleteRoomIn(number: String)

// OUT MODELS

final case class GetUserOut(user: User)
final case class GetUsersOut(users: List[User])

final case class RoomOut(room: Room)
final case class GetRoomsOut(rooms: List[Room])

// ERRORS

sealed trait ErrorType
final case class DbError(msg: String) extends ErrorType
case object NotFound extends ErrorType
case object PasswordInvalid extends ErrorType
case object AlreadyExist extends ErrorType
