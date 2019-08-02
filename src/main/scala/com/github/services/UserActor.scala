package com.github.services

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.github.common._
import com.github.persistence.DbUser

import scala.concurrent.ExecutionContext.Implicits.global

object UserActor {
  def props: Props = Props[UserActor]
  
  final case class GetUser(data: InModels.GetUser)
  final case class CheckPassword(data: InModels.CheckPassword)
  final case class CreateUser(data: InModels.CreateUser)
  final case class UpdateUser(jwt: JwtModel, data: InModels.UpdateUser)
  final case class UpdatePassword(jwt: JwtModel, data: InModels.UpdatePassword)
  final case class DeleteUser(jwt: JwtModel, data: InModels.DeleteUser)
  
  final case class GetUsers(data: InModels.GetUsers)
}

class UserActor extends Actor with ActorLogging {
  import UserActor._
  
  private def ErrorHandler(
    s: ActorRef,
    error: ErrorType,
    notFound: Option[ActorRef => Unit] = None,
    alreadyExists: Option[ActorRef => Unit] = None,
  ): Unit = {
    error match {
      case AlreadyExist => alreadyExists match {
        case Some(f) => f(s)
        case None => s ! Errors.userExists
      }
      case NotFound => notFound match {
        case Some(f) => f(s)
        case None => s ! Errors.userNotFound
      }
      case DbError(msg) => s ! Errors.db(msg)
      case _ => s ! Errors.unknown()
    }
  }
  
  private def DbToApiUser(data: DbModels.User): OutModels.GetUser = {
    OutModels.GetUser(
      email = data.email,
      name = data.name,
      isAdmin = data.isAdmin,
    )
  }
  
  private def DbToApiUsers(data: List[DbModels.User]): OutModels.GetUsers = {
    OutModels.GetUsers(data.map(user => DbToApiUser(user)))
  }
  
  private def GetUserHandler(input: InModels.GetUser): Unit = {
    val s = sender
    DbUser.GetUser(input.email) map {
      case Right(user) => s ! DbToApiUser(user)
      case Left(error) => ErrorHandler(s, error)
    }
  }
  
  
  private def CheckPasswordHandler(input: InModels.CheckPassword): Unit = {
    val s = sender
    DbUser.GetUser(input.email) map {
      case Right(user) =>
        if (Helpers.HashPassword(input.password) == user.passHash)
          s ! DbToApiUser(user)
        else
          s ! Errors.invalidPassword
      case Left(error) => ErrorHandler(s, error)
    }
  }
  
  private def CreateUserHandler(input: InModels.CreateUser): Unit = {
    val s = sender
    DbUser.GetUser(input.email) map {
      case Right(_) => s ! Errors.userExists
      case Left(error) => ErrorHandler(s, error, notFound = Option(s => {
        DbUser.CreateUser(DbModels.User(
          email = input.email,
          passHash = Helpers.HashPassword(input.password),
          name = input.name,
          isAdmin = input.isAdmin
        )) map {
          case None => s ! Messages.created
          case Some(error) => ErrorHandler(s, error)
        }
      }))
    }
  }
  
  private def UpdateUserHandler(jwt: JwtModel, input: InModels.UpdateUser): Unit = {
    val s = sender
    DbUser.GetUser(jwt.email) map {
      case Right(user) =>
        DbUser.UpdateUser(user.email, DbModels.User(
          email = input.email,
          passHash = user.passHash,
          name = input.name,
          isAdmin = input.isAdmin
        )) map {
          case None => s ! Messages.updated
          case Some(error) => ErrorHandler(s, error)
        }
      case Left(error) => ErrorHandler(s, error)
    }
  }
  
  private def UpdatePasswordHandler(jwt: JwtModel, input: InModels.UpdatePassword): Unit = {
    val s = sender
    DbUser.GetUser(jwt.email) map {
      case Right(user) =>
        if (Helpers.HashPassword(input.oldPassword) == user.passHash)
          DbUser.UpdateUser(user.email, DbModels.User(
            email = user.email,
            passHash = Helpers.HashPassword(input.newPassword),
            name = user.name,
            isAdmin = user.isAdmin
          )) map {
            case None => s ! Messages.updated
            case Some(error) => ErrorHandler(s, error)
          }
        else
          s ! Errors.invalidPassword
      case Left(error) => ErrorHandler(s, error)
    }
  }
  
  private def DeleteUserHandler(jwt: JwtModel, input: InModels.DeleteUser): Unit = {
    val s = sender
    DbUser.GetUser(jwt.email) map {
      case Right(user) =>
        if (Helpers.HashPassword(input.password) == user.passHash)
          DbUser.DeleteUser(user.email) map {
            case None => s ! Messages.deleted
            case Some(error) => ErrorHandler(s, error)
          }
        else
          s ! Errors.invalidPassword
      case Left(error) => ErrorHandler(s, error)
    }
  }
  
  private def GetUsersHandler(input: InModels.GetUsers): Unit = {
    val s = sender
    DbUser.GetUsers((input.page - 1) * input.limit, input.limit) map {
      case Right(users) => s ! DbToApiUsers(users)
      case Left(error) => ErrorHandler(s, error)
    }
  }
  
  def receive: Receive = {
    case GetUser(input) => GetUserHandler(input)
    case CheckPassword(input) => CheckPasswordHandler(input)
    case CreateUser(input) => CreateUserHandler(input)
    case UpdateUser(jwt, input) => UpdateUserHandler(jwt, input)
    case UpdatePassword(jwt, input) => UpdatePasswordHandler(jwt, input)
    case DeleteUser(jwt, input) => DeleteUserHandler(jwt, input)
    case GetUsers(input) => GetUsersHandler(input)
  }
}
