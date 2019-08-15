package com.github.persistence

import com.github.common.DbModels
import com.github.persistence.DbRoom._
import org.scalatest.{Assertion, AsyncFunSuite, Matchers}
import scala.concurrent.Future

// TODO: Add messages to fail(msg)

class DbRoomTest extends AsyncFunSuite with Matchers {

    import com.github.persistence.RoomsTestData._
    
    test ("Get Room1 from MongoDB") {
        GetRoom("1") map {
            case Right(r) => r shouldBe roomInDb1
            case Left(_) => fail
        }
    }
    
    test ("Get Rooms(0,0) size from MongoDB should be 2" ) {
        GetRooms(0, 0) map {
            case Right(r) => r.size shouldBe 2
            case Left(_) => fail
        }
    }
    
    test ("Get Rooms(0,0) from MongoDB should be Set(<Room1>, <Room2>)" ) {
        GetRooms(0, 0) map {
            case Right(r) => r shouldBe Set(
                roomInDb1,
                roomInDb2
            )
            case Left(_) => fail
        }
    }

    test ("Get Rooms(1,0) from MongoDB size should be 1" ) {
        GetRooms(1, 0) map {
            case Right(r) => r.size shouldBe 1
            case Left(_) => fail
        }
    }

    test ("Get Rooms(0,1) from MongoDB size should be 1" ) {
        GetRooms(0, 1) map {
            case Right(r) => r.size shouldBe 1
            case Left(_) => fail
        }
    }

    test ("Get Rooms(1,1) from MongoDB size should be 1" ) {
        GetRooms(1, 1) map {
            case Right(r) => r.size shouldBe 1
            case Left(_) => fail
        }
    }

    def checkRoomForCreateRoomTests(number: String, checkedRoom: DbModels.Room): Future[Assertion] = {
        GetRoom(number) map {
            case Right(room) =>
                DeleteRoom("3") // now we can delete created room
                room shouldBe checkedRoom
            case Left(_) =>
                DeleteRoom("3")
                fail
        }
    }
    
    test ("Create Room with zero bookings in MongoDB") {
        val createdRoom = DbModels.Room("3", Set[DbModels.Booking]())

        CreateRoom(createdRoom) flatMap {
            case Some(_) => fail // equal to: case error => fail
            case None => checkRoomForCreateRoomTests("3", createdRoom)
        }
    }

    test ("Create Room with 1 booking in MongoDB") {
        val bkng1 = DbModels.Booking(1262307661L, 1293843661L, "test1@email.com")
        val createdRoom = DbModels.Room("3", Set(bkng1))

        CreateRoom(createdRoom) flatMap {
            case Some(_) => fail // equal to: case error => fail
            case None => checkRoomForCreateRoomTests("3", createdRoom)
        }
    }

    test ("Create Room with 2 bookings in MongoDB") {
        val bkng1 = DbModels.Booking(1272307661L, 1303843661L, "test1@email.com")
        val bkng2 = DbModels.Booking(1372307661L, 1403843661L, "test2@email.com")
        val createdRoom = DbModels.Room("3", Set(bkng1, bkng2))

        CreateRoom(createdRoom) flatMap {
            case Some(_) => fail // equal to: case error => fail
            case None => checkRoomForCreateRoomTests("3", createdRoom)
        }
    }

    test ("FreeRoom") {
        FreeRoom("1", bookingInDb2.start) flatMap {
            case Some(_) => fail
            case None =>
                BookRoom("1", bookingInDb2) map {
                    case Some(_) => fail
                    case None => succeed
                }
        }
    }

    test ("BookRoom in MongoDB") {
        val newBooking = DbModels.Booking(883616461L, 915152461L, "test1@email.com")

        BookRoom("1", newBooking) flatMap {
            case Some(_) => fail
            case None =>
                FreeRoom("1", newBooking.start) flatMap {
                    case Some(_) => fail
                    case None => succeed
                }
        }
    }

    test ("BookRoom in room where bookings is empty in MongoDB") {
        val createdRoom = DbModels.Room("3", Set[DbModels.Booking]())
        
        CreateRoom(createdRoom)

        val newBooking = DbModels.Booking(883616461L, 915152461L, "test1@email.com")
        
        BookRoom("3", newBooking) map {
            case Some(_) =>
                DeleteRoom("3")
                fail
            case None =>
                DeleteRoom("3")
                succeed
        }
    }

    test ("Delete Room from MongoDB") {
        DeleteRoom("1") flatMap {
            case Some(_) => fail
            case None =>
                CreateRoom(roomInDb1)
                succeed
        }
    }
}
