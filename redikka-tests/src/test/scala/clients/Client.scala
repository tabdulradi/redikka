package com.abdulradi.redikka.test.clients

trait Client {
  def set(key: String, value: String): Unit
  def get(key: String): Option[String]
}
