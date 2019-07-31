package com.github.common

import akka.http.scaladsl.model.DateTime

trait JwtTrait {
    val email: String
    val name: String
    val isAdmin: Boolean
}

object Jwt extends JwtTrait {
    val email = "test@mail.ru"
    val name = "Test"
    val isAdmin = true
}

// IN MODELS

case object InModels {
    // User
    final case class GetUser(email: String)
    final case class GetUsers(page: Int, limit: Int)
    final case class CheckPassword(email: String, password: String)
    final case class CreateUser(email: String, password: String, name: String, isAdmin: Boolean)
    final case class UpdateUser(email: String, name: String, isAdmin: Boolean)
    final case class UpdatePassword(oldPassword: String, newPassword: String)
    final case class DeleteUser(password: String)
    
    // Room
    final case class GetRoom(number: String)
    final case class GetRooms(page: Int, limit: Int)
    final case class CreateRoom(number: String)
    final case class BookRoom(number: String, start: DateTime, stop: DateTime)
    final case class FreeRoom(number: String, start: DateTime)
    final case class DeleteRoom(number: String)
}

// OUT MODELS

case object OutModels {
    // Helpers
    final case class Booking(start: DateTime, stop: DateTime, userEmail: String)
    
    // Messages
    final case class Message(code: Int, message: String)
    
    // User
    final case class GetUser(email: String, name: String, isAdmin: Boolean)
    final case class GetUsers(users: List[GetUser])
    
    // Room
    final case class GetRoom(number: String, bookings: List[OutModels.Booking])
    final case class GetRooms(rooms: List[GetRoom])
}

// DB MODELS

case object DbModels {
    // Helpers
    final case class Booking(start: DateTime, stop: DateTime, userEmail: String)
    
    // Real data
    final case class User(email: String, passHash: String, name: String, isAdmin: Boolean)
    final case class Room(number: String, bookings: List[DbModels.Booking])
}

// ERRORS

sealed trait ErrorType

case object NotFound extends ErrorType
case object AlreadyExist extends ErrorType

case object EmailInvalid extends ErrorType
case object PasswordInvalid extends ErrorType

final case class DbError(msg: String) extends ErrorType

// Api Messages

object Messages {
    import OutModels.Message
    
    val ok: Message = Message(0, "ok")
    val created: Message = Message(1, "created")
    val updated: Message = Message(2, "updated")
    val deleted: Message = Message(3, "deleted")
}

object Errors {
    import OutModels.Message
    
    val userNotFound: Message = Message(-1, "user not found")
    val userExists: Message = Message(-2, "user already exists")
    val roomNotFound: Message = Message(-3, "room not found")
    val roomExists: Message = Message(-4, "room already exists")
    val roomBusy: Message = Message(-5, "at selected time room already busy")
    val bookingNotFound: Message = Message(-6, "booking not found")
    val noPermissions: Message = Message(-7, "you have not got permissions")
    
    val invalidEmail: Message = Message(-10, "invalid password")
    val invalidPassword: Message = Message(-11, "invalid password")
    
    val unknown: Message = Message(-98, "unknown error")
    def db(msg: String): Message = Message(-99, s"db error: $msg")
}