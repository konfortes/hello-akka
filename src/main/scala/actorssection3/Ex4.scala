package actorssection3

import akka.actor.ActorSystem
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props

object Ex4 extends App {
  val system = ActorSystem("mySystem")

  object Citizen {
    case class Vote(candidate: String)
    case object VoteStatusRequest
    case class VoteStatusReply(candidate: Option[String])
  }
  class Citizen extends Actor {
    import Citizen._

    def receive: Receive = {
      case Vote(candidate)   => context.become(voted(candidate))
      case VoteStatusRequest => sender() ! None

    }

    def voted(vote: String): Receive = {
      case VoteStatusRequest => sender() ! VoteStatusReply(Some(vote))
    }
  }

  object VoteAggregator {
    case class AggregateVotes(citizens: Set[ActorRef])
  }
  class VoteAggregator extends Actor {
    import Citizen._
    import VoteAggregator._

    def receive: Actor.Receive = awaitingCommand

    def awaitingCommand: Receive = {
      case AggregateVotes(citizens) =>
        citizens.foreach(_ ! VoteStatusRequest)
        context.become(awaitingStatuses(citizens, Map()))
    }

    def awaitingStatuses(
        stillWaiting: Set[ActorRef],
        currentStats: Map[String, Int]
    ): Receive = {
      case VoteStatusReply(None) => sender() ! VoteStatusRequest
      case VoteStatusReply(Some(candidate)) =>
        val newStillWaiting = stillWaiting - sender()
        val newVotesOfCandidate = 1 + currentStats.getOrElse(candidate, 0)
        val newStats: Map[String, Int] =
          currentStats + (candidate -> newVotesOfCandidate)

        if (newStillWaiting.isEmpty) {
          println(s"[aggregator]: vote stats: $newStats")
        } else {
          context.become(awaitingStatuses(newStillWaiting, newStats))
        }

    }

  }

  import Citizen._
  import VoteAggregator._

  val alice = system.actorOf(Props[Citizen], "alice")
  val bob = system.actorOf(Props[Citizen], "bob")
  val john = system.actorOf(Props[Citizen], "jon")
  val james = system.actorOf(Props[Citizen], "james")

  alice ! Vote("gantz")
  bob ! Vote("bibi")
  john ! Vote("lapid")
  james ! Vote("gantz")

  val aggregator = system.actorOf(Props[VoteAggregator], "aggregator")

  aggregator ! AggregateVotes(Set(alice, bob, john, james))
}
