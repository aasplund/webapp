package se.aorwall

import java.io.InputStream
import scala.concurrent.Future
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import se.aorwall.model.Task
import unfiltered.netty.ReceivedMessage
import unfiltered.request._
import unfiltered.response._
import scala.concurrent.ExecutionContext
import scala.util.Success
import scala.util.Failure
import se.aorwall.exceptions.NotFoundException

object WebApp extends App {
  
  import ExecutionContext.Implicits.global
  
  val mapper = new ObjectMapper()
  mapper.registerModule(DefaultScalaModule)
  
  val storage = CassandraStorage
  
  lazy val indexFile = io.Source.fromInputStream(getClass.getResourceAsStream("/index.html")).mkString
  
  lazy val nettyServer = unfiltered.netty.Http(8080)
    .handler(unfiltered.netty.async.Planify({
      case req @ Path(Seg(Nil)) => req.respond(ResponseString(indexFile))
      case req @ Path(Seg("tasks" :: Nil )) => req match {
        case GET(_) => {
          val future = storage.readAll()
          future onComplete {
            case Success(posts) => req.respond(JsonContent ~> anyToResponse(posts))
            case Failure(failure) => failure match {
              case NotFoundException(msg) => req.respond(NotFound ~> anyToResponse(msg))
              case _ => req.respond(InternalServerError)
            }
          }
        }
        case POST(_) => {
          val future = storage.create(requestToPost(req))
          future onComplete {
            case Success(id) => req.respond(JsonContent ~> anyToResponse(id))
            case Failure(failure) => req.respond(InternalServerError)
          }
        }
        case _ => MethodNotAllowed 
      }
      case req @ Path(Seg("tasks" :: id :: Nil )) => req match {
        case GET(_) => {
          val future = storage.read(id)
          future onComplete {
            case Success(post) => req.respond(JsonContent ~> anyToResponse(post))
            case Failure(failure) => failure match {
              case NotFoundException(msg) => req.respond(NotFound ~> anyToResponse(msg))
              case _ => req.respond(InternalServerError)
            }
          }
        }
        case PUT(_) => {
          val future = storage.update(requestToPost(req))
          future onComplete {
            case Success(posts) => req.respond(NoContent)
            case Failure(failure) => failure match {
              case NotFoundException(msg) => req.respond(NotFound ~> anyToResponse(msg))
              case _ => req.respond(InternalServerError)
            }
          }
        }
        case DELETE(_) => {
          val future = storage.delete(id)
          future onComplete {
            case Success(posts) => req.respond(NoContent)
            case Failure(failure) => failure match {
              case NotFoundException(msg) => req.respond(NotFound ~> anyToResponse(msg))
              case _ => req.respond(InternalServerError)
            }
          }
        }
        case _ => MethodNotAllowed
      }
      case _ => NotFound
    }))
    
  nettyServer.run()
  
  protected def requestToPost(req: HttpRequest[ReceivedMessage]): Task = {
     mapper.readValue(Body.stream(req), classOf[Task])
  }
  
  protected def anyToResponse(any: Any): ResponseFunction[Any] = any match {
    case None => NotFound
    case Some(obj) => ResponseString(mapper.writeValueAsString(obj))
    case _ => ResponseString(mapper.writeValueAsString(any))
  }
}