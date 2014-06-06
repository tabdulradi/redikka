package com.abdulradi.redikka.core

import akka.actor.{ Actor, ActorRef, Props, ActorLogging }
import akka.io.{ IO, Tcp }
import akka.util.ByteString
import com.abdulradi.redikka.core.api._

class ValueHolder(key: String) extends Actor with ActorLogging { // Should Be Singleton
  log.debug("Value Holder Actor started at {}", self)
  
  def receive = empty orElse common
  
  def become(behavior: Receive) =
    context.become(behavior orElse common)
  
  def common: Receive = {
    case Set(key, value: Int) =>
      become(holdingNumber(value))
      sender() ! Ok
    case Set(key, value: Double) =>
      become(holdingNumber(value))
      sender() ! Ok
    case Set(key, value) =>
      become(holdingString(value.toString))
      sender() ! Ok 
    case other =>
      log.warning("ValueHolder [{}] of key [{}] got unknown message: {}", self, key, other)
  }
  
  def empty: Receive = {
    case Get(`key`) =>
      sender() ! None
  }
  
  def holdingString(value: String): Receive = {
    case Get(`key`) =>
      sender() ! Some(value)
  }
  
  def holdingNumber(value: Double): Receive = {
    case Get(`key`) =>
      sender() ! Some(value.toString) // convert to String to conform to Redis specs
  }
  
}

object ValueHolder {
  def props(key: String) =
    Props(new ValueHolder(key))
}
