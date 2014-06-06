package com.abdulradi.redikka.tcp

import akka.actor.{ActorSystem, Actor, Terminated, ActorLogging, Props, ActorRef}
import scala.util.control.NonFatal

class App extends Actor with ActorLogging{
  val server = context.system.actorOf(Props(classOf[Server]), "server")
  context watch server
  def receive = {
    case Terminated(server) =>
      log.debug("TCP Server Actor is dead, App Actor will die too")
      context stop self
  }
}

class Terminator(app: ActorRef) extends Actor with ActorLogging {
  context watch app
  def receive = {
    case Terminated(_) ⇒
      log.info("Redikka is shutting down")
      context.system.shutdown()
  }
}

object Main {
  def main(args: Array[String]): Unit = {
    val system = ActorSystem("Main")
    try {
      val app = system.actorOf(Props(classOf[App]), "app")
      system.actorOf(Props(classOf[Terminator], app), "app-terminator")
    } catch {
      case NonFatal(e) =>
        system.shutdown()
        throw e
    }
  }
}
