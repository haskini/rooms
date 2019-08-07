package com.github.persistence

import com.github.common.DbModels
import com.github.persistence.DbUser._
import org.scalatest.{Assertion, AsyncFunSuite, Matchers}
import scala.concurrent.Future

// TODO: Add messages to fail(msg)

class DbUserTest extends AsyncFunSuite with Matchers {

    val userInDb1 = DbModels.User("test1@email.com", "12345", "TestName1", isAdmin = true)
    val userInDb2 = DbModels.User("test2@email.com", "54321", "TestName2", isAdmin = false)
    
    test("Get User1 from MongoDB") {
        GetUser("test1@email.com") map {
            case Right(r) => r shouldBe userInDb1
            case Left(_) => fail
        }
    }
    
    test ("Get Users(0,0) size from MongoDB should be 2" ) {
        GetUsers(0, 0) map {
            case Right(r) => r.size shouldBe 2
            case Left(_) => fail
        }
    }
    
    test ("Get Users(0,0) from MongoDB should be List(<User1>, <User2>)" ) {
        GetUsers(0, 0) map {
            case Right(r) => r shouldBe List(
                userInDb1,
                userInDb2
            )
            case Left(_) => fail
        }
    }
    
    test ("Get Users(1,0) from MongoDB should be List(<User2>)" ) {
        GetUsers(1, 0) map {
            case Right(r) => r shouldBe List(
                userInDb2
            )
            case Left(_) => fail
        }
    }
    
    test ("Get Users(0,1) from MongoDB should be List(<User1>)" ) {
        GetUsers(0, 1) map {
            case Right(r) => r shouldBe List( userInDb1 )
            case Left(_) => fail
        }
    }
    
    test ("Get Users(1,1) from MongoDB should be List(<User2>)" ) {
        GetUsers(1, 1) map {
            case Right(r) => r shouldBe List( userInDb2 )
            case Left(_) => fail
        }
    }
    
    
    def checkUser(email: String, checkedUser: DbModels.User): Future[Assertion] = {
        GetUser(email) map {
            case Right(user) => user shouldBe checkedUser
            case Left(_) => fail
        }
    }
    
    test ("Create User in MongoDB") {
        val createdUser = DbModels.User("createUserEmail@email.com", "09876", "CreatedUser", isAdmin = false)
    
        CreateUser(createdUser) map {
            case Some(_) => fail // equal to: case error => fail
            case None => checkUser("createUserEmail@email.com", createdUser)
        }
    
        DeleteUser("createUserEmail@email.com") map {
            case Some(_) => fail
            case None => succeed
        }
    }
    
    def updateUserInDb2ToOriginal(): Future[Assertion] ={
        UpdateUser("test2@email.com", userInDb2) map {
            case Some(_) => fail
            case None => succeed
        }
    }
    
    test("Update User's isAdmin field in MongoDB") {
        val updatedUser: DbModels.User = userInDb2.copy(isAdmin = true)
        
        UpdateUser("test2@email.com", updatedUser) map {
            case Some(_) => fail
            case None => checkUser("test2@email.com", updatedUser)
        }
        updateUserInDb2ToOriginal()
    }
    
    test("Update User's name field in MongoDB") {
        val updatedUser: DbModels.User = userInDb2.copy(name = "UpdatedUserName")
        
        UpdateUser("test2@email.com", updatedUser) map {
            case Some(_) => fail
            case None => checkUser("test2@email.com", updatedUser)
        }
    
        updateUserInDb2ToOriginal()
    }
    
    test("Update User's passHash field in MongoDB") {
        val updatedUser: DbModels.User = userInDb2.copy(passHash = "UpdatedPassHash")
        
        UpdateUser("test2@email.com", updatedUser) map {
            case Some(_) => fail
            case None => checkUser("test2@email.com", updatedUser)
        }
        
        updateUserInDb2ToOriginal()
    }
    
    test("Update User's email field in MongoDB") {
        val updatedUser: DbModels.User = userInDb2.copy(email = "Updated@email.com")
        
        UpdateUser("test2@email.com", updatedUser) map {
            case Some(_) => fail
            case None => checkUser("test2@email.com", updatedUser)
        }
    
        // and than return original userInDb2
        UpdateUser("Updated@email.com", userInDb2) map {
            case Some(_) => fail
            case None => succeed
        }
    }
    
    test ("Delete User from MongoDB") {
        DeleteUser("test2@email.com") map {
            case Some(_) => fail
            case None => succeed
        }
        
        CreateUser(userInDb2) map {
            case Some(_) => fail
            case None => succeed
        }
    }
}