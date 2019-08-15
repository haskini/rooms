package com.github.api

import akka.actor.ActorRef
import akka.http.scaladsl.model.headers.Cookie
import akka.http.scaladsl.model.{ContentTypes, DateTime, StatusCodes}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.github.common.{DbModels, JwtModel, OutModels}
import com.github.services.{RoomActor, UserActor}
import com.github.persistence.DbRoom._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}
import org.json4s.native.Serialization.read

class RoomRoutesTest extends WordSpec with Matchers with ScalaFutures with ScalatestRouteTest with RoomRoutes with UserRoutes with Routing {
    lazy val routesForRoom: Route = roomRoutes
    override val roomActor: ActorRef = system.actorOf(RoomActor.props, "roomRegistry")
    override val userActor: ActorRef = system.actorOf(UserActor.props, "userRegistry")
    
    "RoomRoutes" should {
        
        // Cookie
        import com.github.persistence.UsersTestData._
        import com.github.persistence.RoomsTestData._
        
        val data = JwtModel(userInDb1.email, userInDb1.name, userInDb1.isAdmin)
        val validToken = generateJwt(data, DateTime.now)
        
        // TEST [/room]
            
        "[Get] /room : Return [signed out] if not authorized" in {
            Get("/room") ~> Route.seal(routesForRoom) ~> check {
                status shouldBe StatusCodes.Unauthorized
                entityAs[String] shouldBe """{"code":-21,"message":"signed out"}"""
            }
        }

        "[Get] /room : Return [invalid number] if no room number" in {
            Get("/room") ~> Cookie("jwt", validToken) ~> Route.seal(routesForRoom) ~> check {
                status shouldBe StatusCodes.BadRequest
                entityAs[String] shouldBe """{"code":-14,"message":"invalid number"}"""
            }
        }

        "[GET] /room?number=1 : Return room1" in {
            Get("/room?number=1") ~> Cookie("jwt", validToken) ~> Route.seal(routesForRoom) ~> check {
                status shouldBe StatusCodes.OK
                val getRoom = read[OutModels.GetRoom](entityAs[String])
                val room = DbModels.Room(getRoom.number, getRoom.bookings.map( b => DbModels.Booking(b.start, b.stop, b.userEmail)))
                room shouldBe roomInDb1
            }
        }
        
        "[POST] /room : Return [signed out] if not authorized" in {
            val request = Post("/room").withEntity(ContentTypes.`application/json`, """{ "number" : "55" }""")
            request ~> Route.seal(routesForRoom) ~> check {
                status shouldBe StatusCodes.Unauthorized
                entityAs[String] shouldBe """{"code":-21,"message":"signed out"}"""
            }
        }
        
        "[POST] /room : Return [already exists] if room already exist" in {
            val request = Post("/room").withEntity(ContentTypes.`application/json`, """{ "number" : "1" }""")
            request ~> Cookie("jwt", validToken) ~> Route.seal(routesForRoom) ~> check {
                status shouldBe StatusCodes.BadRequest
                entityAs[String] shouldBe """{"code":-4,"message":"room already exists"}"""
            }
        }
        
        "[POST] /room : Return OK when create room" in {
            val request = Post("/room").withEntity(ContentTypes.`application/json`, """{ "number" : "55" }""")
            request ~> Cookie("jwt", validToken) ~> Route.seal(routesForRoom) ~> check {
                status shouldBe StatusCodes.Created
                entityAs[String] shouldBe """{"code":1,"message":"created"}"""
            }
            DeleteRoom("55")
        }
        
        "[DELETE] /room : Return [signed out] if not authorized" in {
            val request = Delete("/room").withEntity(ContentTypes.`application/json`, """{ "number" : "1" }""")
            request ~> Route.seal(routesForRoom) ~> check {
                status shouldBe StatusCodes.Unauthorized
                entityAs[String] shouldBe """{"code":-21,"message":"signed out"}"""
            }
        }
    
        "[DELETE] /room : Return [room not found] when deleting non exist room" in {
            val request = Delete("/room").withEntity(ContentTypes.`application/json`, """{ "number" : "55" }""")
            request ~> Cookie("jwt", validToken) ~> Route.seal(routesForRoom) ~> check {
                status shouldBe StatusCodes.BadRequest
                entityAs[String] shouldBe """{"code":-3,"message":"room not found"}"""
            }
        }
        
        "[DELETE] /room : Return OK when delete room" in {
            val request = Delete("/room").withEntity(ContentTypes.`application/json`, """{ "number" : "1" }""")
            request ~> Cookie("jwt", validToken) ~> Route.seal(routesForRoom) ~> check {
                status shouldBe StatusCodes.OK
                entityAs[String] shouldBe """{"code":3,"message":"deleted"}"""
            }
            CreateRoom(roomInDb1)
        }
        
        // TEST [/booking]
        
        // TODO fix [POST] booking
        "[POST] /booking : Return [signed out] if not authorized" ignore {
        
        }
    
        // TODO fix [POST] booking
        "[POST] /booking :  Return [invalid booking] if no booking in room list" ignore {
        
        }
    
        // TODO fix [POST] booking
        "[POST] /booking : Return OK if was booked" ignore {
        
        }
        
        // TODO fix [DELETE booking]
        "[DELETE] /booking : Return [signed out] if not authorized" ignore {

        }

        // TODO fix [DELETE] booking
        "[DELETE] /booking : Return [room not found] when deleting non exist room" ignore {

        }
    
        // TODO fix [DELETE] booking
        "[DELETE] /booking : Return [start not fount] when deleting non exist booking" ignore {
        
        }
        
        // TEST [/rooms]
        
        "[GET] /rooms : Return [signed out] if not authorized" in {
            Get("/rooms") ~> Route.seal(routesForRoom) ~> check {
                status shouldBe StatusCodes.Unauthorized
                entityAs[String] shouldBe """{"code":-21,"message":"signed out"}"""
            }
        }
    
        // TODO fix [GET] rooms
        "[GET] /rooms?page=0&limit=0 : Return all rooms" ignore {
        
        }
        
        // TODO make some tests with page=X&limit=Y
    }
}
