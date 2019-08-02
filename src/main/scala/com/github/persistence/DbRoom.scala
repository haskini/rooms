package com.github.persistence

import akka.http.scaladsl.model.DateTime
import com.github.common.DbModels.{Room, User}
import com.github.common._
import org.json4s.DefaultFormats
import org.json4s.native.JsonMethods.parse
import org.json4s.native.Serialization.write
import org.mongodb.scala.{Document, MongoCollection}
import org.mongodb.scala.model.Filters.equal

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object DbRoom {
    
    implicit val formats: DefaultFormats.type = DefaultFormats
    val roomsCollection: MongoCollection[Document] = MongoFactory.database.getCollection("rooms")
    
    def GetRoom(number: String): Future[Either[ErrorType, Room]] = {
        val documentsSeqFuture: Future[Seq[Document]] = roomsCollection.find(equal("number", number)).toFuture()
        val documentEitherFuture: Future[Either[ErrorType, Room]] = documentsSeqFuture.map({
            value: Seq[Document] => Right(parse(value.head.toJson).extract[Room])
        }).recover{
            case _ => Left(NotFound)
        }
        documentEitherFuture
    }
    
    def GetRooms(skip: Int, limit: Int): Future[Either[ErrorType, List[Room]]] = {
        val documentsSkipLimitSeqFuture: Future[Seq[Document]] = roomsCollection.find().limit(limit).skip(skip).toFuture()
        documentsSkipLimitSeqFuture.map({
            value: Seq[Document] => Right(value.map(doc => parse(doc.toJson).extract[Room]).toList)
        }).recover{
            case _ => Left(NotFound)
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
    
    def BookRoom(number: String, newData: DbModels.Booking): Option[ErrorType] = {
        None
    }
    
    def FreeRoom(number: String, start: DateTime): Option[ErrorType] = {
        None
    }
}
