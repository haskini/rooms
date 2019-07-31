package com.github.persistence

import com.github.common._

object DbUser {
    
    def GetUser(data: GetUserIn): Either[ErrorType, Int] = {
        Left(NotFound)
    }
    
    def GetUsers(): GetUsersOut = {
        GetUsersOut(List())
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
