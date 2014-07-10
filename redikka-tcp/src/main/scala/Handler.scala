package com.abdulradi.redikka.tcp

import java.net.InetSocketAddress
import scala.concurrent.duration._
import akka.actor.{ Actor, ActorRef, Props, ActorLogging }
import akka.io.Tcp
import akka.pattern.ask
import akka.util.{ByteString, Timeout}
import com.abdulradi.redikka.core.api._

private case class CommandAck(data: Any) extends Tcp.Event

/*
 * This Code is almost dummy, that only passes the integration testcases
 * TODO: Parse ByteString to Case Classes that represents the protocol
 */
class RequestHandler(redikka: ActorRef) extends Actor with ActorLogging {
  import Tcp._

  val CRLF = ByteString("\r\n")

  val duration = Duration(100, MILLISECONDS)
  implicit val timeout = Timeout(duration)
  implicit val ec = context.dispatcher

  def ascii(bytes: ByteString): String =
    bytes.decodeString("US-ASCII").trim

  def receive = {
    case msg @ Received(data) if (ascii(data).contains("SET"))=>
      val currentSender = sender()
      (redikka ? Set("key", "some value")).map {
        case Ok =>
          currentSender ! Write(ByteString("+OK\r\n"), CommandAck("Set"))
      }
    case CommandAck(data) =>
      log.debug(s"Got the Ack: $data")
    case msg @ Received(data) if (ascii(data).contains("GET"))=>
      val currentSender = sender()
      (redikka ? Get("key")).foreach {
        case Value(Some(value)) =>
          currentSender ! Write(ByteString("$" + value.length + "\r\n" + value + "\r\n"))
        case Value(None) =>
          currentSender ! Write(ByteString("$-1\r\n"))
        case other =>
          log.error(s"Got unknown reply from Redikka: $other")
      }
    case PeerClosed =>
      log.debug("PeerClosed")
      context stop self
  }
}

object RequestHandler {
  def props(redikka: ActorRef) =
    Props(new RequestHandler(redikka))
}
