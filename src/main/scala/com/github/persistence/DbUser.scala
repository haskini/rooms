package com.github.persistence

import com.github.common._
import com.github.common.DbModels.User
import org.json4s._
import org.json4s.native.Serialization._
import org.json4s.native.JsonMethods._
import org.mongodb.scala.{ Document, MongoCollection}
import org.mongodb.scala.model.Filters._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object DbUser {
    
    implicit val formats: DefaultFormats.type = DefaultFormats
    val userCollection: MongoCollection[Document] = MongoFactory.database.getCollection("users")
    
    def GetUser(email: String): Future[Either[ErrorType, User]] = {
        val documentsSeqFuture: Future[Seq[Document]] = userCollection.find(equal("email", email)).toFuture()
        val documentEitherFuture: Future[Either[ErrorType, User]] = documentsSeqFuture.map({
            value: Seq[Document] => Right(parse(value.head.toJson).extract[User])
        }).recover{
            case _ => Left(NotFound)
        }
        documentEitherFuture
    }
    
    def GetUsers(skip: Int, limit: Int): Future[Either[ErrorType, List[User]]] = {
        val documentsSkipLimitSeqFuture: Future[Seq[Document]] = userCollection.find().limit(limit).skip(skip).toFuture()
        documentsSkipLimitSeqFuture.map({
            value: Seq[Document] => Right(value.map(doc => parse(doc.toJson).extract[User]).toList)
        }).recover{
            case _ => Left(NotFound)
        }
    }

    // returns None if was created
    def CreateUser(newData: User): Future[Option[ErrorType]] = {
        val createUserJson = write(newData)
        val userDoc: Document = Document(createUserJson)
        userCollection.insertOne(userDoc).toFuture().map({
             _ => None
        }).recover{
            case _ => Some(DbError("[DB ERROR] Can't create user"))
        }
    }

    // update all user data by old email (besides password)
    def UpdateUser(email: String, newData: User): Future[Option[ErrorType]] = {
        val newDataJson = write(newData)
        val newDoc: Document = Document(newDataJson)
        
        userCollection.replaceOne(equal("email", email), newDoc).toFuture().map({
             _ => None
        }).recover {
            case _ => Some(DbError("[DB ERROR] Can't update user"))
        }
    }

    def DeleteUser(email: String): Future[Option[ErrorType]] = {
        userCollection.deleteOne(equal("email", email)).toFuture().map({
            _ => None
        }).recover{
            case _ => Some(DbError("[DB ERROR] Can't delete user"))
        }
    }
}