package com.github.persistence

import akka.http.scaladsl.model.DateTime
import com.github.common.DbModels
import com.github.persistence.DbRoom._
import org.scalatest.{Assertion, AsyncFunSuite, Matchers}
import scala.concurrent.Future

// TODO: Add messages to fail(msg)

class DbRoomTest extends AsyncFunSuite with Matchers {

    import com.github.persistence.RoomsTestData._
    
    ignore ("Get Room1 from MongoDB") {
        GetRoom("1") map {
            case Right(r) => r shouldBe roomInDb1
            case Left(_) => fail
        }
    }
    
    ignore ("Get Rooms(0,0) size from MongoDB should be 2" ) {
        GetRooms(0, 0) map {
            case Right(r) => r.size shouldBe 2
            case Left(_) => fail
        }
    }
    
    ignore ("Get Rooms(0,0) from MongoDB should be List(<Room1>, <Room2>)" ) {
        GetRooms(0, 0) map {
            case Right(r) => r shouldBe List(
                roomInDb1,
                roomInDb2
            )
            case Left(_) => fail
        }
    }
    
    ignore ("Get Rooms(1,0) from MongoDB should be List(<Room2>)" ) {
        GetRooms(1, 0) map {
            case Right(r) => r shouldBe List( roomInDb2 )
            case Left(_) => fail
        }
    }
    
    ignore ("Get Rooms(0,1) from MongoDB should be List(<Room1>)" ) {
        GetRooms(0, 1) map {
            case Right(r) => r shouldBe List( roomInDb1 )
            case Left(_) => fail
        }
    }
    
    ignore ("Get Rooms(1,1) from MongoDB should be List(<Room2>)" ) {
        GetRooms(1, 1) map {
            case Right(r) => r shouldBe List( roomInDb2 )
            case Left(_) => fail
        }
    }
    
    def checkRoom(number: String, checkedRoom: DbModels.Room): Future[Assertion] = {
        GetRoom(number) map {
            case Right(room) => room shouldBe checkedRoom
            case Left(_) => fail
        }
    }
    
    def deleteRoom(number: String): Future[Assertion] = {
        DeleteRoom(number) map {
            case Some(_) => fail
            case None => succeed
        }
    }
    
    test ("Create Room with zero bookings in MongoDB") {
        val createdRoom = DbModels.Room("3", List())
        
        CreateRoom(createdRoom) map {
            case Some(_) => fail // equal to: case error => fail
            case None => checkRoom("3", createdRoom)
        }
        
        deleteRoom("3")
    }
    
    test ("Create Room with 1 booking in MongoDB") {
        val bkng1 = DbModels.Booking(DateTime(5553334400000L), DateTime(6660489600000L), "test1@email.com")
        val createdRoom = DbModels.Room("3", List(bkng1))
        
        CreateRoom(createdRoom) map {
            case Some(_) => fail // equal to: case error => fail
            case None => checkRoom("3", createdRoom)
        }
    
        deleteRoom("3")
    }
    
    test ("Create Room with 2 bookings in MongoDB") {
        val bkng1 = DbModels.Booking(DateTime(4443334400000L), DateTime(5550489600000L), "test1@email.com")
        val bkng2 = DbModels.Booking(DateTime(4448640000000L), DateTime(5550176000000L), "test2@email.com")
        val createdRoom = DbModels.Room("3", List(bkng1, bkng2))
        
        CreateRoom(createdRoom) map {
            case Some(_) => fail // equal to: case error => fail
            case None => checkRoom("3", createdRoom)
        }
    
        deleteRoom("3")
    }
    
    test ("Delete Room from MongoDB") {
        DeleteRoom("1") map {
            case Some(_) => fail
            case None => succeed
        }
        
        CreateRoom(roomInDb1) map {
            case Some(_) => fail
            case None => succeed
        }
    }
    
    test ("FreeRoom") {
        FreeRoom("1", bookingInDb2.start) map {
            case Some(_) => fail
            case None => succeed
        }
        
        BookRoom("1", bookingInDb2) map {
            case Some(_) => fail
            case None => succeed
        }
    }
    
    test ("BookRoom in MongoDB") {
        val newBooking = DbModels.Booking(DateTime(6000489600000L), DateTime(7000489600000L), "test1@email.com")
    
        BookRoom("1", newBooking) map {
            case Some(_) => fail
            case None => succeed
        }
        
        FreeRoom("1", newBooking.start) map {
            case Some(_) => fail
            case None => succeed
        }
    }
    
    test ("BookRoom in room where bookings is empty in MongoDB") {
        FreeRoom("2", bookingInDb3.start) map {
            case Some(_) => fail
            case None => succeed
        }
        // now bookings list is empty
        
        val newBooking = DbModels.Booking(DateTime(6000489600000L), DateTime(7000489600000L), "test1@email.com")
        BookRoom("2", newBooking) map {
            case Some(_) => fail
            case None => succeed
        }
    
        // return deleted booking
        BookRoom("2", bookingInDb3) map {
            case Some(_) => fail
            case None => succeed
        }
        // delete created booking
        FreeRoom("2", newBooking.start) map {
            case Some(_) => fail
            case None => succeed
        }
    }
    
}
