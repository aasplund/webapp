package se.aorwall

import scala.concurrent.Future

trait Storage[Id, T] {
  
  def create(obj: T): Future[String]
  
  def read(id: Id): Future[T]
  def readAll(): Future[List[T]]
  
  def update(obj: T): Future[Unit]
  
  def delete(id: Id): Future[Unit]
  
}