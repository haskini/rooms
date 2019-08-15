package com.github.persistence

import akka.http.scaladsl.model.DateTime
import com.github.common.DbModels

object UsersTestData {
    val userInDb1 = DbModels.User("test1@email.com", "12345", "TestName1", isAdmin = true)
    val userInDb2 = DbModels.User("test2@email.com", "54321", "TestName2", isAdmin = false)
}

object RoomsTestData {
    val bookingInDb1 = DbModels.Booking(DateTime(1993334400000L), DateTime(2340489600000L), "test1@email.com")
    val bookingInDb2 = DbModels.Booking(DateTime(1898640000000L), DateTime(1930176000000L), "test2@email.com")
    val bookingInDb3 = DbModels.Booking(DateTime(2003334400000L), DateTime(2013334400000L), "test2@email.com")
    
    val roomInDb1 = DbModels.Room("1", List(bookingInDb1, bookingInDb2))
    val roomInDb2 = DbModels.Room("2", List(bookingInDb3))
}