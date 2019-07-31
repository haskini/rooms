package com.github.common

// IN MODELS

final case class dbUser(email: String, name: String, isAdmin: String, password: String)
final case class GetUserIn(email: String)
final case class CheckPasswordIn(email: String, password: String)
final case class CreateUserIn(email: String, name: String, isAdmin: String, password: String)
final case class UpdateUserIn(oldEmail: String, email: String, name: String, isAdmin: String)
final case class UpdatePasswordIn(email: String, oldPassword: String, newPassword: String)
final case class DeleteUserIn(email: String, password: String)

// OUT MODELS

final case class GetUserOut(email: String, password: String, name: String, isAdmin: String)
final case class GetUsersOut(users: List[GetUserOut])

// ERRORS

sealed trait ErrorType
final case class DbError(msg: String) extends ErrorType
case object NotFound extends ErrorType
case object PasswordInvalid extends ErrorType
case object AlreadyExist extends ErrorType
