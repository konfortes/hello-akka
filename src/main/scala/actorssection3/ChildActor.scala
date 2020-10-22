package actorssection3

import akka.actor.ActorSystem
import akka.actor.Actor
import akka.actor.Props
import akka.actor.ActorRef

object ChildActor extends App {
  val system = ActorSystem("mySystem")

  object Parent {
    case class CreateChild(name: String)
    case class TellChild(message: String)
  }

  class Parent extends Actor {
    import Parent._

    def receive: Actor.Receive = {
      case CreateChild(name) =>
        println(s"[${self.path}] creating child $name")
        val childRef = context.actorOf(Props[Child], name)
        context.become(hasChildReceive(childRef))
    }

    def hasChildReceive(child: ActorRef): Receive = {
      case TellChild(msg) => child forward (msg)
    }
  }

  class Child extends Actor {
    def receive: Actor.Receive = {
      case msg: String => println(s"[${self.path}] got message! $msg")
    }
  }

  import Parent._

  val parent = system.actorOf(Props[Parent], "parent")
  parent ! CreateChild("yaya")
  parent ! TellChild("bagaya")

  val childSelection = system.actorSelection("/user/parent/yaya")
  childSelection ! "found you!"
}
