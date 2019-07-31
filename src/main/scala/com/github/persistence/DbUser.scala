package com.github.persistence

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

// Errors

sealed trait ErrorType
final case class DbError(msg: String) extends ErrorType
case object NotFound extends ErrorType
case object PasswordInvalid extends ErrorType
case object AlreadyExist extends ErrorType


object DbUser {
    
    def GetUser(data: GetUserIn): Either[ErrorType, Int] = {
        Left(NotFound)
    }
    
    def GetUsers(): List[GetUserOut] = {
        List()
    }
    
    // returns None if correct
    def CheckPassword(data: CheckPasswordIn): Option[ErrorType] = {
        None
    }
    
    // returns None if was created
    def CreateUser(data: CreateUserIn): Option[ErrorType] = {
        None
    }
    
    // update all user data by old email (besides password)
    def UpdateUser(data: UpdateUserIn): Option[ErrorType] = {
        None
    }
    
    // update pass by email
    def UpdatePassword(data: UpdatePasswordIn): Option[ErrorType] = {
        None
    }
    
    def DeleteUser(data: DeleteUserIn): Option[ErrorType] = {
        None
    }
}
