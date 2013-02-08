package se.aorwall.exceptions

case class NotFoundException(message: String) extends Exception(message) {

}