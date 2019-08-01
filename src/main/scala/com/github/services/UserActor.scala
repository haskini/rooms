package com.github.services

import akka.actor.{Actor, ActorLogging, Props}
import com.github.common._
import com.github.persistence.DbUser

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
  
  def receive: Receive = {
    case GetUser(input) =>
      DbUser.GetUser(input.email) match {
        case Right(user) => sender() ! user
        case Left(error) =>
          error match {
            case NotFound => sender() ! Errors.userNotFound
            case DbError(msg) => sender() ! Errors.db(msg)
            case _ => sender() ! Errors.unknown
          }
      }
    case CheckPassword(input) =>
      DbUser.GetUser(input.email) match {
        case Right(user) =>
          if (Helpers.HashPassword(input.password) == user.passHash)
            sender() ! Messages.ok
          else
            sender() ! Errors.invalidPassword
        case Left(error) =>
          error match {
            case NotFound => sender() ! Errors.userNotFound
            case DbError(msg) => sender() ! Errors.db(msg)
            case _ => sender() ! Errors.unknown
          }
      }
    case CreateUser(input) =>
      DbUser.GetUser(input.email) match {
        case Right(_) =>
          sender() ! Errors.userExists
        case Left(gettingError) =>
          gettingError match {
            case NotFound =>
              DbUser.CreateUser(DbModels.User(
                email = input.email,
                passHash = Helpers.HashPassword(input.password),
                name = input.name,
                isAdmin = input.isAdmin
              )) match {
                case None => sender() ! Messages.created
                case Some(error) =>
                  error match {
                    case DbError(msg) => sender() ! Errors.db(msg)
                    case _ => sender() ! Errors.unknown
                  }
              }
            case DbError(msg) => sender() ! Errors.db(msg)
            case _ => sender() ! Errors.unknown
          }
      }
    case UpdateUser(jwt, input) =>
      DbUser.GetUser(jwt.email) match {
        case Right(user) =>
          DbUser.UpdateUser(user.email, DbModels.User(
            email = input.email,
            passHash = user.passHash,
            name = input.name,
            isAdmin = input.isAdmin
          )) match {
            case None => sender() ! Messages.updated
            case Some(error) =>
              error match {
                case DbError(msg) => sender() ! Errors.db(msg)
                case _ => sender() ! Errors.unknown
              }
          }
        case Left(error) =>
          error match {
            case NotFound => sender() ! Errors.userNotFound
            case DbError(msg) => sender() ! Errors.db(msg)
            case _ => sender() ! Errors.unknown
          }
      }
    case UpdatePassword(jwt, input) =>
      DbUser.GetUser(jwt.email) match {
        case Right(user) =>
          if (Helpers.HashPassword(input.oldPassword) == user.passHash)
            DbUser.UpdateUser(user.email, DbModels.User(
              email = user.email,
              passHash = Helpers.HashPassword(input.newPassword),
              name = user.name,
              isAdmin = user.isAdmin
            )) match {
              case None => sender() ! Messages.updated
              case Some(error) =>
                error match {
                  case DbError(msg) => sender() ! Errors.db(msg)
                  case _ => sender() ! Errors.unknown
                }
            }
          else
            sender() ! Errors.invalidPassword
        case Left(error) =>
          error match {
            case NotFound => sender() ! Errors.userNotFound
            case DbError(msg) => sender() ! Errors.db(msg)
            case _ => sender() ! Errors.unknown
          }
      }
    case DeleteUser(jwt, input) =>
      DbUser.GetUser(jwt.email) match {
        case Right(user) =>
          if (Helpers.HashPassword(input.password) == user.passHash)
            DbUser.DeleteUser(user.email) match {
              case None => sender() ! Messages.deleted
              case Some(error) =>
                error match {
                  case DbError(msg) => sender() ! Errors.db(msg)
                  case _ => sender() ! Errors.unknown
                }
            }
          else
            sender() ! Errors.invalidPassword
        case Left(error) =>
          error match {
            case NotFound => sender() ! Errors.userNotFound
            case DbError(msg) => sender() ! Errors.db(msg)
            case _ => sender() ! Errors.unknown
          }
      }
    case GetUsers(input) =>
      DbUser.GetUsers((input.page - 1) * input.limit, input.limit) match {
        case Right(users) => sender() ! users
        case Left(error) =>
          error match {
            case DbError(msg) => sender() ! Errors.db(msg)
            case _ => sender() ! Errors.unknown
          }
      }
  }
}
