package com.abdulradi.redikka.core

import akka.actor.{ Actor, ActorRef, Props, ActorLogging }
import akka.io.{ IO, Tcp }
import akka.util.ByteString
import com.abdulradi.redikka.core.api.KeyCommand

class Redikka extends Actor with ActorLogging { // Should Be Singleton on Node. Used by Consistent Hashing Router
  log.debug("Redikka Singleton Actor started at {}", self)
  
  def getValueHolder(key: String): ActorRef = 
    context.child(key) getOrElse newValueHolder(key)
    
  def newValueHolder(key: String): ActorRef = 
    context.actorOf(ValueHolder.props(key), key)
  
  def receive = {
    case op: KeyCommand =>
      getValueHolder(op.key) forward op
  }
}

object Redikka {
  def props = 
    Props[Redikka]
}
