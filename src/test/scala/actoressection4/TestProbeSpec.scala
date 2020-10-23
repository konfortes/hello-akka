package actoressection4

import org.scalatest.BeforeAndAfterAll
import org.scalatest.WordSpecLike
import akka.testkit.ImplicitSender
import akka.testkit.TestKit
import akka.actor.ActorSystem

class TestProbeSpec
    extends TestKit(ActorSystem("BasicSpec"))
    with ImplicitSender
    with WordSpecLike
    with BeforeAndAfterAll {
  override def afterAll(): Unit = TestKit.shutdownActorSystem(system)

  "something" should {
    "do x when given y" in {
      assert(1 == 1)
    }
  }

  "something" should {
    "do x when given y" in {
      assert(1 == 1)
    }
  }
}
