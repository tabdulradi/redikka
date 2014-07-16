package com.abdulradi.redikka.tcp

import java.net.InetSocketAddress
import akka.actor.{ActorRef, Actor, ActorLogging}
import akka.io.{IO, Tcp}
import com.abdulradi.redikka.core.Redikka

class Server extends Actor with ActorLogging {

  import Tcp._

  implicit val system = context.system

  val port = Server.defaultPort
  log.info("TCP Server has started, trying to listen on port {}", port)
  IO(Tcp) ! Bind(self, new InetSocketAddress("localhost", port))
  val redikka: ActorRef = Redikka(context.system)

  def receive = {
    case Bound(localAddress) =>
      log.info("TCP Server successfully bound on address {}", localAddress)
    case msg@CommandFailed(_: Bind) =>
      log.error("TCP Server failed to bind on port {}.", port)
      context stop self
    case c@Connected(remote, local) =>
      log.debug("TCP Server got a new connection remote={} local={}", remote, local)
      sender() ! Register(context.actorOf(RequestHandler.props(redikka)))
  }
}

object Server {
  val defaultPort = 9736
}
