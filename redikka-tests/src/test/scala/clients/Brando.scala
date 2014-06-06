package com.abdulradi.redikka.test.clients

import scala.concurrent.duration._
import scala.concurrent.Await
import scala.reflect.ClassTag

import akka.util.{ByteString, Timeout}
import akka.actor.ActorSystem
import akka.pattern.ask 

import brando.{Request, StatusReply}

case class Brando (port: Int) extends Client {
  val system = ActorSystem("BrandoTestClient")
  val duration = Duration(100, MILLISECONDS)
  implicit val timeout = Timeout(duration) 

  def asAscii(bytes: ByteString): String =
    bytes.decodeString("US-ASCII").trim
    
  val redis = system.actorOf(brando.Brando("localhost", port))
  
  def request[T:ClassTag](request: Request): T = 
    Await.result((redis ? request).mapTo[T], duration)
  

  def set(key: String, value: String): Unit =
    request[Some[StatusReply]](Request("SET", key, value))

  def get(key: String): Option[String] =
    request[Option[ByteString]](Request("GET", key)).map(asAscii)
  
}
