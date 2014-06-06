package com.abdulradi.redikka.test.clients

case class ScalaRedis(port: Int) extends Client {
  val r = new com.redis.RedisClient("localhost", port)

  def set(key: String, value: String): Unit =
    r.set(key, value)

  def get(key: String): Option[String] =
    r.get(key)
}
