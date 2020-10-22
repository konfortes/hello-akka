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

  class StatelesFussyKid extends Actor {
    import FussyKid._
    import Mom._
    override def receive: Actor.Receive = happyReceive

    def happyReceive: Receive = {
      case Food(VEGETABLES) => context.become(sadReceive)
      case Food(CHOCOLATE)  =>
      case Ask(_)           => sender() ! Accept
    }
    def sadReceive: Receive = {
      case Food(VEGETABLES) =>
      case Food(CHOCOLATE)  => context.become(happyReceive)
      case Ask(_)           => sender() ! Reject
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
      case MomStart(kid) =>
        kid ! Food(VEGETABLES)
        kid ! Ask("go to bed")
        kid ! Food(CHOCOLATE)
        kid ! Ask("go to bed")
      case Accept => println("hey ho")
      case Reject => println("damn")
    }
  }

  val mom = system.actorOf(Props[Mom], "mom")
  val kid = system.actorOf(Props[FussyKid], "kid")
  val statelessKid = system.actorOf(Props[FussyKid], "statelessKid")

//   mom ! Mom.MomStart(kid)
  mom ! Mom.MomStart(statelessKid)
}
