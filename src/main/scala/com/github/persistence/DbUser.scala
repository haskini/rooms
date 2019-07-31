package com.github.persistence

import com.github.common._



object DbUser {
    def GetUser(email: String): Either[ErrorType, DbModels.User] = {
        Left(NotFound)
    }

    def GetUsers(skip: Int, limit: Int): Either[ErrorType, List[DbModels.User]] = {
        Right(List())
    }

    // returns None if was created
    def CreateUser(newData: DbModels.User): Option[ErrorType] = {
        None
    }

    // update all user data by old email (besides password)
    def UpdateUser(email: String, newData: DbModels.User): Option[ErrorType] = {
        None
    }

    def DeleteUser(email: String): Option[ErrorType] = {
        None
    }
}
