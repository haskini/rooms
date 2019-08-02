package com.github.persistence

import akka.http.scaladsl.model.DateTime
import com.github.common.DbModels.{Booking, Room}
import com.github.common._
import org.bson.json.JsonWriterSettings
import org.json4s.DefaultFormats
import org.json4s.native.Serialization._
import org.json4s.native.JsonMethods._
import org.mongodb.scala.bson.BsonInt64
import org.mongodb.scala.{Document, MongoCollection}
import org.mongodb.scala.model.Filters.equal
import org.mongodb.scala.model.Updates._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

protected final case class DbBooking(start: Long, stop: Long, userEmail: String)
protected final case class DbRoom(number: String, bookings: List[DbBooking])

private object Converter {
    def BookingToDbBooking(booking: Booking): DbBooking = {
        DbBooking(booking.start.clicks, booking.stop.clicks, booking.userEmail)
    }
    
    def DbBookingToBooking(dbBooking: DbBooking): Booking = {
        val start: DateTime = DateTime(dbBooking.start)
        val stop = DateTime(dbBooking.stop)
        Booking(start, stop, dbBooking.userEmail)
    }
    
    def RoomToDbRoom(room: Room): DbRoom = {
        DbRoom(room.number, room.bookings.map(BookingToDbBooking))
    }
    
    def DbRoomToRoom(dbRoom: DbRoom): Room = {
        Room(dbRoom.number, dbRoom.bookings.map(DbBookingToBooking))
    }
}

object DbRoom {
    
    implicit val formats: DefaultFormats.type = DefaultFormats
    val roomsCollection: MongoCollection[Document] = MongoFactory.database.getCollection("rooms")
    
    def GetRoom(number: String): Future[Either[ErrorType, Room]] = {
        val documentsSeqFuture: Future[Seq[Document]] = roomsCollection.find(equal("number", number)).toFuture()
        documentsSeqFuture.map{
            value: Seq[Document] =>
                val settings: JsonWriterSettings = JsonWriterSettings.builder()
                    .int64Converter((v, writer) => writer.writeNumber(v.toString)).build()
                
                val json = value.head.toJson(settings)
                val dbRoom = parse(json).extract[DbRoom]
                Right(Converter.DbRoomToRoom(dbRoom))
        }.recover{
            case _ => Left(NotFound)
        }
    }
    
    def GetRooms(skip: Int, limit: Int): Future[Either[ErrorType, List[Room]]] = {
        roomsCollection.find().limit(limit).skip(skip).toFuture().map{
            documents: Seq[Document] =>
                val dbRoomsList: List[DbRoom] = documents.map{ doc =>
                    parse(doc.toJson).extract[DbRoom] // Convert Documents from Seq to DbRoom Objects
                }.toList // and create List of DbRoom's
                Right(dbRoomsList.map(Converter.DbRoomToRoom)) // Then convert all DbRoom's to Room's
        }.recover{
            case _ => Left(NotFound)
        }
    }
    
    // returns None if was created
    def CreateRoom(newData: Room): Future[Option[ErrorType]] = {
        val createRoomJson = write(Converter.RoomToDbRoom(newData))
        val roomDoc: Document = Document(createRoomJson)
        roomsCollection.insertOne(roomDoc).toFuture().map({
            _ => None
        }).recover{
            case _ => Some(DbError("[DB ERROR] Can't create room"))
        }
    }
    
    def DeleteRoom(number: String): Future[Option[ErrorType]] = {
        roomsCollection.deleteOne(equal("number", number)).toFuture().map({
            _ => None
        }).recover{
            case _ => Some(DbError("[DB ERROR] Can't delete room"))
        }
    }
    
    def BookRoom(number: String, newData: Booking): Future[Option[ErrorType]] = {
        val dbBooking = Converter.BookingToDbBooking(newData)
        val dbBookingDoc = Document(write(dbBooking)).toBsonDocument
        roomsCollection.findOneAndUpdate( equal("number", number),
            push("bookings", dbBookingDoc)).toFuture().
            map {
            _ => None
        }.recover{
            case _ => Some(DbError("[DB ERROR] Can't book room"))
        }
    }
    
    def FreeRoom(number: String, start: DateTime): Future[Option[ErrorType]] = {
        roomsCollection.findOneAndUpdate(equal("number", number),
            pull("bookings", equal("start", BsonInt64(start.clicks)))
        ).toFuture().map {
            _ => None
        }.recover {
            case _ => Some(DbError("[DB ERROR] Can't free room"))
        }
    }
}