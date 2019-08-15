package com.github.persistence

import com.github.common.DbModels

object UsersTestData {
    val userInDb1 = DbModels.User("test1@email.com", "12345", "TestName1", isAdmin = true)
    val userInDb2 = DbModels.User("test2@email.com", "54321", "TestName2", isAdmin = false)
}

object RoomsTestData {
    val bookingInDb1 = DbModels.Booking(1104541261L, 1136077261L, "test1@email.com")
    val bookingInDb2 = DbModels.Booking(1167613261L, 1199149261L, "test2@email.com")
    val bookingInDb3 = DbModels.Booking(946688461L, 978310861L, "test2@email.com")
    
    val roomInDb1 = DbModels.Room("1", Set(bookingInDb1, bookingInDb2))
    val roomInDb2 = DbModels.Room("2", Set(bookingInDb3))
}