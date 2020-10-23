package actoressection4

import akka.testkit.TestKit
import akka.testkit.ImplicitSender
import akka.actor.ActorSystem
import akka.actor.Actor
import akka.actor.Props
import org.scalatest.WordSpecLike
import org.scalatest.BeforeAndAfterAll
import scala.concurrent.duration._

class BasicSpec
    extends TestKit(ActorSystem("BasicSpec"))
    with ImplicitSender
    with WordSpecLike
    with BeforeAndAfterAll {
  import BasicSpec._
  override def afterAll(): Unit = TestKit.shutdownActorSystem(system)

  "echo actor" should {
    "echo messages" in {
      val echoActor = system.actorOf(Props[SimpleActor], "echoActor")
      val msg = "Hello There"
      echoActor ! msg

      expectMsg(msg)
    }
  }

  "blackhole actor" should {
    "send no message back" in {
      val echoActor =
        system.actorOf(Props[BlackholeActor], "blackholeActor")
      val msg = "Hello There"
      echoActor ! msg

      expectNoMessage(1 second)
    }
  }

  "uppercase actor" should {
    val upperActor = system.actorOf(Props[UpperActor], "upperActor")
    "upper a message" in {
      upperActor ! "akka"

      val reply = expectMsgType[String]
      assert(reply == "AKKA")
    }
  }

}

object BasicSpec {
  class SimpleActor extends Actor {
    override def receive: Actor.Receive = {
      case msg => sender() ! msg
    }
  }

  class BlackholeActor extends Actor {
    override def receive: Actor.Receive = {
      case msg => // do nothing
    }
  }

  class UpperActor extends Actor {
    def receive: Actor.Receive = {
      case msg: String => sender() ! msg.toUpperCase()
    }
  }
}
