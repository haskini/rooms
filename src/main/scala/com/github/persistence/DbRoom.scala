package com.github.persistence

import akka.http.scaladsl.model.DateTime
import com.github.common._

object DbRoom {
    def GetRoom(number: String): Either[ErrorType, DbModels.Room] = {
        Left(NotFound)
    }
    
    def GetRooms(skip: Int, limit: Int): Either[ErrorType, List[DbModels.Room]] = {
        Right(List())
    }
    
    // returns None if was created
    def CreateRoom(newData: DbModels.Room): Option[ErrorType] = {
        None
    }
    
    def DeleteRoom(number: String): Option[ErrorType] = {
        None
    }
    
    def BookRoom(number: String, newData: DbModels.Booking): Option[ErrorType] = {
        None
    }
    
    def FreeRoom(number: String, start: DateTime): Option[ErrorType] = {
        None
    }
}
