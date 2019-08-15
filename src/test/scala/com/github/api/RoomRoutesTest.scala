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
        
        "[POST] /booking : Return [signed out] if not authorized" in {
            val request = Post("/booking").withEntity(ContentTypes.`application/json`,
                """{"number": "1", "start": 1565860887, "stop": 1597483287}""")
            request ~> Route.seal(routesForRoom) ~> check {
                status shouldBe StatusCodes.Unauthorized
                entityAs[String] shouldBe """{"code":-21,"message":"signed out"}"""
            }
        }
    
        "[POST] /booking : Return [room not found] if room doesn't exist" in {
            val request = Post("/booking").withEntity(ContentTypes.`application/json`,
                """{"number": "3", "start": 1565860887, "stop": 1597483287}""")
            request ~> Cookie("jwt", validToken) ~> Route.seal(routesForRoom) ~> check {
                status shouldBe StatusCodes.BadRequest
                entityAs[String] shouldBe """{"code":-3,"message":"room not found"}"""
            }
        }
    
        "[POST] /booking : Return OK if was booked" in {
            val request = Post("/booking").withEntity(ContentTypes.`application/json`,
                """{"number": "1", "start": 1565860887, "stop": 1597483287}""")
            request ~> Cookie("jwt", validToken) ~> Route.seal(routesForRoom) ~> check {
                status shouldBe StatusCodes.OK
                entityAs[String] shouldBe """{"code":2,"message":"updated"}"""
            }
            FreeRoom("1", 1565860887L)
        }
    
        "[POST] /booking : Return [at selected time room already busy] if attempt to book in booked time" in {
            val request = Post("/booking").withEntity(ContentTypes.`application/json`,
                """{"number": "1", "start": 1104541262, "stop": 1136077260}""")
            request ~> Cookie("jwt", validToken) ~> Route.seal(routesForRoom) ~> check {
                status shouldBe StatusCodes.BadRequest
                entityAs[String] shouldBe """{"code":-5,"message":"at selected time room already busy"}"""
            }
        }
    
        "[POST] /booking : Return [at selected time room already busy] if attempt to book in booked time (start earlier, stop in interval)" in {
            val request = Post("/booking").withEntity(ContentTypes.`application/json`,
                """{"number": "1", "start": 1104541260, "stop": 1136077260}""")
            request ~> Cookie("jwt", validToken) ~> Route.seal(routesForRoom) ~> check {
                status shouldBe StatusCodes.BadRequest
                entityAs[String] shouldBe """{"code":-5,"message":"at selected time room already busy"}"""
            }
        }
    
        "[POST] /booking : Return [at selected time room already busy] if attempt to book in booked time (start in interval, stop later)" in {
            val request = Post("/booking").withEntity(ContentTypes.`application/json`,
                """{"number": "1", "start": 1104541262, "stop": 1136077262}""")
            request ~> Cookie("jwt", validToken) ~> Route.seal(routesForRoom) ~> check {
                status shouldBe StatusCodes.BadRequest
                entityAs[String] shouldBe """{"code":-5,"message":"at selected time room already busy"}"""
            }
        }
    
        "[DELETE] /booking : Return [signed out] if not authorized" in {
            val request = Delete("/booking").withEntity(ContentTypes.`application/json`,
                """{"number": "2", "start": 1564844400}""")
            request ~> Route.seal(routesForRoom) ~> check {
                status shouldBe StatusCodes.Unauthorized
                entityAs[String] shouldBe """{"code":-21,"message":"signed out"}"""
            }
        }

        "[DELETE] /booking : Return [room not found] when deleting non exist room" in {
            val request = Delete("/booking").withEntity(ContentTypes.`application/json`,
                """{"number": "3", "start": 1104541262}""")
            request ~> Cookie("jwt", validToken) ~> Route.seal(routesForRoom) ~> check {
                status shouldBe StatusCodes.BadRequest
                entityAs[String] shouldBe """{"code":-3,"message":"room not found"}"""
            }
        }
    
        "[DELETE] /booking : Return [booking not found] when deleting non exist booking" in {
            val request = Delete("/booking").withEntity(ContentTypes.`application/json`, """{"number": "1", "start": 1104541262}""")
            request ~> Cookie("jwt", validToken) ~> Route.seal(routesForRoom) ~> check {
                status shouldBe StatusCodes.BadRequest
                entityAs[String] shouldBe """{"code":-6,"message":"booking not found"}"""
            }
        }
        
        "[DELETE] /booking : Return OK if was deleted" in {
            val request = Delete("/booking").withEntity(ContentTypes.`application/json`,
                """{"number": "1", "start": 1104541261}""")
            request ~> Cookie("jwt", validToken) ~> Route.seal(routesForRoom) ~> check {
                status shouldBe StatusCodes.OK
                entityAs[String] shouldBe """{"code":2,"message":"updated"}"""
            }
            DeleteRoom("1")
            CreateRoom(roomInDb1)
        }
        
        // TEST [/rooms]
        
        "[GET] /rooms : Return [signed out] if not authorized" in {
            Get("/rooms") ~> Route.seal(routesForRoom) ~> check {
                println(entityAs[String])
                status shouldBe StatusCodes.Unauthorized
                entityAs[String] shouldBe """{"code":-21,"message":"signed out"}"""
            }
        }
        
        "[GET] /rooms : Return all rooms" ignore {
            Get("/rooms") ~> Cookie("jwt", validToken) ~> Route.seal(routesForRoom) ~> check {
                status shouldBe StatusCodes.OK
                println(entityAs[String])
//                val setRooms = parse(entityAs[String]).extract[List[OutModels.GetRoom]]
//                setRooms shouldBe Set(roomInDb1, roomInDb2)
            }
        }
    
        "[GET] /rooms?page=0&limit=0 : Return all rooms" ignore {
            Get("/rooms?page=0&limit=0") ~> Cookie("jwt", validToken) ~> Route.seal(routesForRoom) ~> check {
                status shouldBe StatusCodes.OK
                println(entityAs[String])
//                val setRooms = read[OutModels.GetRoom](entityAs[String])
//                setRooms shouldBe Set(roomInDb1, roomInDb2)
            }
        }
    
        "[GET] /rooms?page=1&limit=0 : Return set with size eq 1" ignore {
            Get("/rooms?page=1&limit=0") ~> Cookie("jwt", validToken) ~> Route.seal(routesForRoom) ~> check {
                status shouldBe StatusCodes.OK
                println(entityAs[String])
//                val setRooms = read[Set[OutModels.GetRoom]](entityAs[String])
//                setRooms.size shouldBe 1
            }
        }
    
        "[GET] /rooms?page=0&limit=1 : Return set with size eq 1" ignore {
            Get("/rooms?page=0&limit=1") ~> Cookie("jwt", validToken) ~> Route.seal(routesForRoom) ~> check {
                status shouldBe StatusCodes.OK
                println(entityAs[String])
//                val setRooms = read[Set[OutModels.GetRoom]](entityAs[String])
//                setRooms.size shouldBe 1
            }
        }

    }
}
