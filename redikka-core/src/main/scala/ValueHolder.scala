package com.abdulradi.redikka.core

import akka.event.LoggingReceive

import scala.concurrent.duration._
import scala.util.{Success, Try, Failure}
import akka.actor.Actor.Receive
import akka.actor._
import akka.persistence.{SnapshotOffer, PersistentActor, RecoveryCompleted}
import akka.util.ByteString
import com.abdulradi.redikka.core.api._

class ValueHolder(key: String) extends PersistentActor with ActorLogging { // Should Be Singleton per key
  import ValueHolder._
  implicit val ec = context.dispatcher
  log.debug("ValueHolder({}) started with key {}, now recovering...", self, key)

  var state: State = State(None)

  override def persistenceId = s"redikka-value-$key"

  def updateState: Event => Unit = {
    case ValueSet(v) =>
      state = state.copy(value=Some(v))
    case Idled =>
      state = state.copy(hasClearance = true) // Flag, that it is safe to continue working next time the Actor is created.
  }

  val receiveRecover: Receive = LoggingReceive {
    case e: Event =>
      log.debug("ValueHolder({}) got event while recovering: {}", self, e)
      updateState(e)
    case SnapshotOffer(_, snapshot: State) =>
      log.debug("ValueHolder({}) got snapshot offer while recovering: {}", self, snapshot)
      state = snapshot
    case RecoveryCompleted if state.hasClearance => // We are safe to work now, just revoke clearance
      log.debug("ValueHolder({}) finished recovering, and got clearance.", self)
      state = state.copy(hasClearance = false)
      // TODO: this should be event too
    case RecoveryCompleted =>
      log.debug("ValueHolder({}) finished recovering, but doesn't have clearance. This means that either another actor is running on different node, or Actor crashed before. In both cases, we are waiting for Clearance, to continue, if it didn't come within timeout we will continue anyway", self)
      val timeout = context.system.scheduler.scheduleOnce(5 seconds, self, ClearanceTimeout)
      context.become(waitingForClearance(timeout))
    case msg =>
      log.debug("ValueHolder({}) received unknown message while recovering: {}", self, msg)
  }

  def waitingForClearance(timeout: Cancellable): Receive = {
    case AccessCleared =>
      log.debug("ValueHolder({}) received Clearance message.", self)
      killOldTimeoutAndBecomeReadyToExecute(timeout)
    case ClearanceTimeout =>
      log.warning("ValueHolder({}) timeout during waiting for Clearance message, it is Ok ONLY if Actor crashed in the last time. Else we may need to change the timeout duration.", self)
      becomeReadyToExecute()
  }

  def newIdleTimeout() =
    context.system.scheduler.scheduleOnce(5 seconds, self, IdleTimeout)

  def becomeReadyToExecute() =
    context.become(executeCommand(newIdleTimeout))


  def killOldTimeoutAndBecomeReadyToExecute(timeout: Cancellable) = {
    timeout.cancel()
    becomeReadyToExecute()
  }

  def executeCommand(timeout: Cancellable): Receive = LoggingReceive {
    case Get =>
      sender ! state.value
      killOldTimeoutAndBecomeReadyToExecute(timeout)
    case cmd: KeyCommand =>
      val event = validateWriteCommand(cmd)
      persist(event) {
        case Success(e) =>
          updateState(e)
        case Failure(e) =>
          ??? // TODO
      }
      killOldTimeoutAndBecomeReadyToExecute(timeout)
    case IdleTimeout =>
      persist(Idled)(updateState)
      self ! PoisonPill
      context.become(dying)
    case cmd =>
      log.debug(s"Received Unknown command [$cmd]")
  }

  val validateWriteCommand: RedikkaCommand => Try[Event] = {
    case Get(_) => ??? // This is required to remove compiler warning, TODO: I should create another super type for ReadWriteCommands
    case Set(_, v) =>
      ???
  }

  val receiveCommand: Receive =
    executeCommand(newIdleTimeout)

  def dying: Receive = LoggingReceive {
    case cmd: KeyCommand =>
      ???
      /* TODO:
       Akhh! What should we do?
       1. Halt shutdown procedure, comeback to live again, set clearance to false.
          Problems:
            If cluster rebalanced in this very moment, there is a risk that similar actor is created on another node,
            and has the clearance now!

       2. Forward the command back to NodeManager (parent), hopefully we will be dead already when it process them.
          Parent should crate a new actor now
          Problem:
            What happens to other messages in inbox after the poison pill??

       3. Inform parent first that we want to die, wait for it's approval.
          Parent marks this actor as toBeKilled, and send Die message. Waits for it to be really killed.
          Any message arrived in between, should be stashed by parent.
          Problems:
          What happens if cluster re-balance in the same time?
            Parent would be stashing messages that may belong to other node.
              Should we resend them again to the Consistent Hashing Router to be sure?
                But this means that later messages (from client) may have arrive the new Node BEFORE old messages trapped here!
       */
  }
}

object ValueHolder {
  def props(key: String) =
    Props(new ValueHolder(key))

  case class State(value: Option[String] = None, hasClearance: Boolean = true)

  case object AccessCleared
  case object ClearanceTimeout
  case object IdleTimeout

  sealed trait Event
  case object Idled extends Event
  case class ValueSet(value: String) extends Event
}
