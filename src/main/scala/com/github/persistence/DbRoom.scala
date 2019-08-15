package com.github.persistence

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

object DbRoom {
    
    implicit val formats: DefaultFormats.type = DefaultFormats
    val roomsCollection: MongoCollection[Document] = MongoFactory.database.getCollection("rooms")
    
    val settings: JsonWriterSettings = JsonWriterSettings.builder()
        .int64Converter((v, writer) => writer.writeNumber(v.toString)).build()
    
    def GetRoom(number: String): Future[Either[ErrorType, Room]] = {
        val documentsSeqFuture: Future[Seq[Document]] = roomsCollection.find(equal("number", number)).toFuture()
        documentsSeqFuture.map{
            case Seq() => Left(NotFound)
            case value: Seq[Document] =>
                val json = value.head.toJson(settings)
                Right(parse(json).extract[Room])
        }.recover{
            case _ => Left(DbError("[DB ERROR] Can't get room"))
        }
    }
    
    def GetRooms(skip: Int, limit: Int): Future[Either[DbError, Set[Room]]] = {
        roomsCollection.find().limit(limit).skip(skip).toFuture().map{
            case Seq() => Right(Set[Room]())
            case documents: Seq[Document] =>
                val roomsSet: Set[Room] = documents.map{ doc =>
                    parse(doc.toJson(settings)).extract[Room] // Convert Documents from Seq to DbRoom Objects
                }.toSet // and create Set of DbRoom's
                Right(roomsSet) // Then convert all DbRoom's to Room's
        }.recover{
            case _ => Left(DbError("[DB ERROR] Can't get rooms"))
        }
    }
    
    // returns None if was created
    def CreateRoom(newData: Room): Future[Option[ErrorType]] = {
        val createRoomJson = write(newData)
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
        val dbBookingDoc = Document(write(newData)).toBsonDocument
        roomsCollection.findOneAndUpdate( equal("number", number),
            push("bookings", dbBookingDoc)).toFuture().
            map {
            _ => None
        }.recover{
            case _ => Some(DbError("[DB ERROR] Can't book room"))
        }
    }
    
    def FreeRoom(number: String, start: Long): Future[Option[ErrorType]] = {
        roomsCollection.findOneAndUpdate(equal("number", number),
            pull("bookings", equal("start", BsonInt64(start)))
        ).toFuture().map {
            _ => None
        }.recover {
            case _ => Some(DbError("[DB ERROR] Can't free room"))
        }
    }
}