package com.abdulradi.redikka.tcp

import scala.util.control.NonFatal
import scala.concurrent.duration._
import com.typesafe.config.ConfigFactory
import akka.actor.{ActorSystem, Actor, Terminated, ActorLogging, Props,
  ActorRef, ActorPath, Identify, ActorIdentity }
import akka.persistence.journal.leveldb.{SharedLeveldbJournal, SharedLeveldbStore}
import akka.pattern.ask
import akka.util.Timeout
import com.abdulradi.redikka.core.Redikka

class App extends Actor with ActorLogging {
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
  implicit val timeout = Timeout(15.seconds)

  def startTCPServer(system: ActorSystem): Unit =
    try {
      val app = system.actorOf(Props(classOf[App]), "app")
      system.actorOf(Props(classOf[Terminator], app), "app-terminator")
    } catch {
      case NonFatal(e) =>
        system.shutdown()
        throw e
    }

  def main(args: Array[String]): Unit = {
    val systems =
      if (args.isEmpty)
        startup(Seq("0", "2551", "2552"))
      else
        startup(args)

    startTCPServer(systems(0))
  }

  def startupSharedJournal(system: ActorSystem, startStore: Boolean, path: ActorPath): Unit = {
    // Start the shared journal one one node (don't crash this SPOF)
    // This will not be needed with a distributed journal

    if (startStore)
      system.actorOf(Props[SharedLeveldbStore], "store")
    // register the shared journal
    import system.dispatcher

    val f = (system.actorSelection(path) ? Identify(None))
    f.onSuccess {
      case ActorIdentity(_, Some(ref)) => SharedLeveldbJournal.setStore(ref, system)
      case _ =>
        system.log.error("Shared journal not started at {}", path)
        system.shutdown()
    }
    f.onFailure {
      case _ =>
        system.log.error("Lookup of shared journal at {} timed out", path)
        system.shutdown()
    }
  }

  def startup(ports: Seq[String]): Seq[ActorSystem] =
    ports map { port =>
      // Override the configuration of the port
      val config =
        ConfigFactory.parseString(s"akka.remote.netty.tcp.port=$port").
          withFallback(ConfigFactory.load())

      // Create an Akka system
      val system = ActorSystem("ClusterSystem", config)
      val isPrimaryNode = (port == "2551")
      startupSharedJournal(
        system,
        startStore=isPrimaryNode, path =
        ActorPath.fromString("akka.tcp://ClusterSystem@127.0.0.1:2551/user/store"))

      Redikka.init(system)
      system
    }
}
