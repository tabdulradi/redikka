package com.abdulradi.redikka.test

import com.abdulradi.redikka.test.clients.{ScalaRedis, Brando}

abstract class StringOpsSpec(c: TestConfig) extends Spec(c) {
  subject when {
    "set value" should {
      "be able to get it back" in {
        r.set("key", "some value")
        r.get("key") shouldBe Some("some value")
      }
    }
  }
}

//class StringOpsSpecWithScalaRedisOnRedis extends StringOpsSpec(Redis via ScalaRedis)
class StringOpsSpecWithScalaRedisOnRedikka extends StringOpsSpec(Redikka via ScalaRedis)
//class StringOpsSpecWithBrandoOnRedis extends StringOpsSpec(Redis via Brando)
//class StringOpsSpecWithBrandoOnRedikka extends StringOpsSpec(Redikka via Brando)

