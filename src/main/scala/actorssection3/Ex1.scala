package actorssection3

import akka.actor.ActorSystem
import akka.actor.Actor
import akka.actor.Props

object Ex1 extends App {
  val system = ActorSystem("myActorSystem")

  class CounterActor extends Actor {
    var count = 0
    def receive: Actor.Receive = {
      case "inc"   => count += 1
      case "dec"   => count -= 1
      case "print" => println(s"[${self.path}] counter is set to $count")
    }
  }

  val counterRef = system.actorOf(Props[CounterActor], "myCounter")

  counterRef ! "inc"
  counterRef ! "inc"
  counterRef ! "dec"
  counterRef ! "dec"
  counterRef ! "inc"
  counterRef ! "print"
}
