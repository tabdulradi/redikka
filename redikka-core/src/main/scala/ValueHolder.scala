package com.abdulradi.redikka.core

import akka.event.LoggingReceive

import scala.concurrent.duration._
import scala.util.{Success, Try, Failure}
import akka.actor.Actor.Receive
import akka.actor._
import akka.persistence.{SnapshotOffer, PersistentActor, RecoveryCompleted}
import akka.contrib.pattern.ShardRegion.Passivate
import akka.util.ByteString
import com.abdulradi.redikka.core.api._

class ValueHolder extends PersistentActor with ActorLogging {
  import ValueHolder._
  implicit val ec = context.dispatcher

  log.debug("New ValueHolder started at ({}), now recovering.", self)

  override def persistenceId: String = self.path.parent.name + "-" + self.path.name

  var state: State = State(None)

  context.setReceiveTimeout(2.minutes)

  def updateState: Event => Unit = {
    case ValueSet(v) =>
      val newState = state.copy(value=Some(v))
      log.debug("ValueHolder({}) changing state from {} to {}", self, state, newState)
      state = newState
    case event =>
      log.debug("ValueHolder({}) unknown event {}", self, event)
      ???
  }

  def receiveRecover: Receive = LoggingReceive {
    case e: Event =>
      log.debug("ValueHolder({}) got event while recovering: {}", self, e)
      updateState(e)
    case SnapshotOffer(_, snapshot: State) =>
      log.debug("ValueHolder({}) got snapshot offer while recovering: {}", self, snapshot)
      state = snapshot
    case msg =>
      log.debug("ValueHolder({}) received unknown message while recovering: {}", self, msg)
  }

  val validateRWCommand: KeyRWCommand => Try[Event] = {
    case Set(_, v) =>
      Success(ValueSet(v.toString)) // TODO: Handle numbers
    case other =>
      Failure(new UnsupportedCommand(other))
  }

  def receiveCommand: Receive = LoggingReceive {
    case Get(_) =>
      val value = Value(state.value)
      log.debug("ValueHolder({}) got Get Command replying with ({}), to sender ({})", self, value, sender())
      sender() ! value
    case cmd: KeyRWCommand =>
      validateRWCommand(cmd) match {
        case Success(event) =>
          log.debug("ValueHolder({}) successfully validated cmd ({}), to event ({})", self, cmd, event)
          persist(event) { e =>
            log.debug("ValueHolder({}) successfully persisted event ({})", self, cmd, event)
            updateState(e)
            sender() ! Ok
          }
        case Failure(e) =>
          log.debug("ValueHolder({}) failed to validate cmd ({}). Error was ({})", self, cmd, e)
          ???
      }
  }

  override def unhandled(msg: Any): Unit = msg match {
    case ReceiveTimeout =>
      log.debug("ValueHolder({}) got ReceiveTimeout for being idle.", self)
      context.parent ! Passivate(stopMessage = PoisonPill)
    case other =>
      log.debug("ValueHolder({}) got unknown command ({})", self, other)
      super.unhandled(msg)
  }
}

object ValueHolder {
  def props(): Props =
    Props(new ValueHolder)

  class UnsupportedCommand(cmd: Any) extends Exception

  case class State(value: Option[String] = None)

  sealed trait Event

  @SerialVersionUID(1L)
  case class ValueSet(value: String) extends Event
}
