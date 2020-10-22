package actorssection3

import akka.actor.ActorSystem
import akka.actor.Actor
import akka.actor.Props
import akka.actor.AbstractActor.Receive

object Ex3 extends App {
  val system = ActorSystem("mySystem")

  object Counter {
    case object Increment
    case object Decrement
    case object Print
  }

  class Counter extends Actor {
    import Counter._

    def receive: Receive = countReceive(0)

    def countReceive(currentCount: Int): Receive = {
      case Increment => context.become(countReceive(currentCount + 1))
      case Decrement => context.become(countReceive(currentCount - 1))
      case Print     => println(s"my current count is $currentCount")
    }
  }

  import Counter._
  val counter = system.actorOf(Props[Counter], "myCounter")

  counter ! Increment
  counter ! Increment
  counter ! Increment
  counter ! Decrement
  counter ! Decrement

  counter ! Print
}
