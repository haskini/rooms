package com.github.common

import akka.http.scaladsl.model.DateTime

// IN MODELS

case object InModels {
    // User
    final case class GetUser(email: String)
    final case class GetUsers(page: Int, limit: Int)
    final case class CheckPassword(email: String, password: String)
    final case class CreateUser(email: String, password: String, name: String, isAdmin: Boolean)
    final case class UpdateUser(oldEmail: String, email: String, name: String, isAdmin: Boolean)
    final case class UpdatePassword(email: String, oldPassword: String, newPassword: String)
    final case class DeleteUser(email: String, password: String)
    
    // Room
    final case class GetRoom(number: Int)
    final case class GetRooms(page: Int, limit: Int)
    final case class CreateRoom(number: String)
    final case class BookRoom(number: String, start: DateTime, stop: DateTime, userEmail: String)
    final case class FreeRoom(number: String, start: DateTime)
    final case class DeleteRoom(number: String)
}

// OUT MODELS

case object OutModels {
    // Helpers
    final case class Booking(start: DateTime, stop: DateTime, userEmail: String)
    
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
    final case class User(email: String, passHash: Option[String], name: Option[String], isAdmin: Option[Boolean])
    final case class Room(number: String, bookings: Option[List[DbModels.Booking]])
}

// ERRORS

sealed trait ErrorType

case object NotFound extends ErrorType
case object AlreadyExist extends ErrorType

case object EmailInvalid extends ErrorType
case object PasswordInvalid extends ErrorType

final case class DbError(msg: String) extends ErrorType
