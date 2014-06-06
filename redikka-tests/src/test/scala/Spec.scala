package com.abdulradi.redikka.test

import org.scalatest._
import com.abdulradi.redikka.test.clients.Client

abstract class Spec(config: TestConfig) extends WordSpec with Matchers {
  def r = config.r
  def subject = config.subject
}

case class TestConfig(r: Client, subject: String)

object Redis {
  def via(client: Int => Client) = 
    TestConfig(client(6379), "Redis")
}

object Redikka {
  def via(client: Int => Client) = 
    TestConfig(client(9736), "Redikka")
}

