package com.abdulradi.redikka.core

import akka.actor.{ ActorSystem }
import akka.io.{ IO, Tcp }
import akka.util.ByteString
import akka.contrib.pattern.{ShardRegion, ClusterSharding}
import com.abdulradi.redikka.core.api.KeyCommand

object Redikka {
  val ShardName = "RedikkaValueHolder"

  protected val idExtractor: ShardRegion.IdExtractor = {
    case cmd: KeyCommand =>
      (cmd.key, cmd)
  }

  protected val shardResolver: ShardRegion.ShardResolver = msg => msg match {
    case cmd: KeyCommand =>
      (math.abs(cmd.key.hashCode) % 100).toString
  }

  def init(implicit system: ActorSystem) = {
    system.log.debug("Redikka starting initialization. ShardName={}", ShardName)
    ClusterSharding(system).start(
      typeName = ShardName,
      entryProps = Some(ValueHolder.props),
      idExtractor = idExtractor,
      shardResolver = shardResolver)
    system.log.debug("Redikka initialization complete")
  }

  def apply(implicit system: ActorSystem) =
    ClusterSharding(system).shardRegion(ShardName)
}
