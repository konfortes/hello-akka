package actorssection3

import akka.actor.ActorSystem
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props

object ChangingBehavior extends App {
  val system = ActorSystem("mySystem")

  object FussyKid {
    case object Accept
    case object Reject
    val HAPPY = "happy"
    val SAD = "sad"
  }

  class FussyKid extends Actor {
    import FussyKid._
    import Mom._

    var state = HAPPY
    override def receive: Actor.Receive = {
      case Food(VEGETABLES) => state = SAD
      case Food(CHOCOLATE)  => state = HAPPY
      case Ask(_) =>
        if (state == HAPPY) sender() ! Accept
        else sender() ! Reject
    }
  }

  object Mom {
    case class MomStart(kid: ActorRef)
    case class Food(food: String)
    case class Ask(message: String)
    val VEGETABLES = "veggies"
    val CHOCOLATE = "chocolate"
  }

  class Mom extends Actor {
    import Mom._
    import FussyKid._

    override def receive: Actor.Receive = {
      case s: String => println(s)
      case MomStart(kid) =>
        println("Starting!")
        kid ! Food(VEGETABLES)
        kid ! Ask("go to bed")
        kid ! Food(CHOCOLATE)
        kid ! Ask("go to bed")
      case Accept => println("hey ho")
      case Reject => println("damn")
    }
    val mom = system.actorOf(Props[Mom], "mom")
    val kid = system.actorOf(Props[FussyKid], "kid")

    mom ! "WTF"
    mom ! MomStart(kid)
  }

}
