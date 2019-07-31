package com.github.persistence

import com.github.common._

object DbRoom {
    def GetRoom(data: GetRoomIn): Either[ErrorType, RoomOut] = {
        Left(NotFound)
    }
    
    def CreateRoom(data: CreateRoomIn): Option[ErrorType] = {
        None
    }
    
    def BookRoom(data: BookRoomIn): Option[ErrorType] = {
        None
    }
    
    def FreeRoom(data: FreeRoomIn): Option[ErrorType] = {
        None
    }
    
    def DeleteRoom(data: DeleteRoomIn): Option[ErrorType] = {
        None
    }
    
    def GetRooms(): Either[ErrorType, GetRoomsOut]  = {
        Right(GetRoomsOut(List()))
    }
}
