package com.github.persistence

import org.mongodb.scala._

object MongoFactory {
    val mongoClient: MongoClient = MongoClient("mongodb://localhost")
    val database: MongoDatabase = mongoClient.getDatabase("rooms")
}