package com.github.persistence

import com.github.common.DbModels
import com.github.persistence.DbUser._
import org.scalatest.{Assertion, AsyncFunSuite, Matchers}
import scala.concurrent.Future

// TODO: Add messages to fail(msg)

class DbUserTest extends AsyncFunSuite with Matchers {
    
    import com.github.persistence.UsersTestData._
    
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
    
    test ("Get Users(0,0) from MongoDB should be Set(<User1>, <User2>)" ) {
        GetUsers(0, 0) map {
            case Right(r) => r shouldBe Set(
                userInDb1,
                userInDb2
            )
            case Left(_) => fail
        }
    }
    
    test ("Get Users(1,0) from MongoDB should be Set(<User2>)" ) {
        GetUsers(1, 0) map {
            case Right(r) => r shouldBe Set(
                userInDb2
            )
            case Left(_) => fail
        }
    }
    
    test ("Get Users(0,1) from MongoDB should be Set(<User1>)" ) {
        GetUsers(0, 1) map {
            case Right(r) => r shouldBe Set( userInDb1 )
            case Left(_) => fail
        }
    }
    
    test ("Get Users(1,1) from MongoDB should be Set(<User2>)" ) {
        GetUsers(1, 1) map {
            case Right(r) => r shouldBe Set( userInDb2 )
            case Left(_) => fail
        }
    }
    
    
    def checkUserForCreateUser(email: String, checkedUser: DbModels.User): Future[Assertion] = {
        GetUser(email) map {
            case Right(user) =>
                DeleteUser(email)
                user shouldBe checkedUser
            case Left(_) =>
                DeleteUser(email)
                fail
        }
    }
    
    test ("Create User in MongoDB") {
        val createdUser = DbModels.User("createUserEmail@email.com", "09876", "CreatedUser", isAdmin = false)
    
        CreateUser(createdUser) flatMap  {
            case Some(_) => fail // equal to: case error => fail
            case None => checkUserForCreateUser("createUserEmail@email.com", createdUser)
        }
    }
    
    def checkUserForUpdateUser(updatedUser: DbModels.User, originalUser: DbModels.User): Future[Assertion] ={
        GetUser(updatedUser.email) flatMap {
            case Right(user) =>
                DeleteUser(updatedUser.email)
                CreateUser(originalUser)
                user shouldBe updatedUser
            case Left(_) =>
                DeleteUser(updatedUser.email)
                CreateUser(originalUser)
                fail
        }
    }
    
    test("Update User's isAdmin field in MongoDB") {
        val updatedUser: DbModels.User = userInDb2.copy(isAdmin = true)
        
        UpdateUser("test2@email.com", updatedUser) flatMap {
            case Some(_) => fail
            case None => checkUserForUpdateUser(updatedUser, userInDb2)
        }
    }
    
    test("Update User's name field in MongoDB") {
        val updatedUser: DbModels.User = userInDb2.copy(name = "UpdatedUserName")
        
        UpdateUser("test2@email.com", updatedUser) flatMap {
            case Some(_) => fail
            case None => checkUserForUpdateUser(updatedUser, userInDb2)
        }
    }
    
    test("Update User's passHash field in MongoDB") {
        val updatedUser: DbModels.User = userInDb2.copy(passHash = "UpdatedPassHash")
        
        UpdateUser("test2@email.com", updatedUser) flatMap  {
            case Some(_) => fail
            case None => checkUserForUpdateUser(updatedUser, userInDb2)
        }
    }
    
    test("Update User's email field in MongoDB") {
        val updatedUser: DbModels.User = userInDb2.copy(email = "Updated@email.com")
        
        UpdateUser("test2@email.com", updatedUser) flatMap {
            case Some(_) => fail
            case None => checkUserForUpdateUser(updatedUser, userInDb2)
        }
    }
    
    test ("Delete User from MongoDB") {
        DeleteUser("test2@email.com") flatMap {
            case Some(_) => fail
            case None =>
                CreateUser(userInDb2)
                succeed
        }
    }
}