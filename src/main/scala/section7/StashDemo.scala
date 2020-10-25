package section7

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.Stash
import akka.actor.ActorSystem
import akka.actor.Props

object StashDemo extends App {

  case object Open
  case object Close
  case object Read
  case class Write(data: String)

  class ResourceActor extends Actor with ActorLogging with Stash {
    private var innerData: String = ""

    def receive: Receive = closeReceive

    def closeReceive: Receive = {
      case Open =>
        log.info("handling Open")
        unstashAll()
        context.become(openReceive)
      case msg =>
        log.info(s"stashing $msg since it can not be handled right now")
        stash()
    }

    def openReceive: Receive = {
      case Close =>
        log.info("closing")
        unstashAll()
        context.become(closeReceive)
      case Read => log.info("Read: " + innerData)
      case Write(data) =>
        log.info(s"handling write of $data")
        innerData = data
      case msg =>
        log.info(s"can't handle msg currently. stashing it")
        stash()
    }
  }

  val system = ActorSystem("mySystem")
  val resourceActor = system.actorOf(Props[ResourceActor], "resourceActor")

  resourceActor ! Read
  resourceActor ! Open
  resourceActor ! Open
  resourceActor ! Write("akka")
  resourceActor ! Close
  resourceActor ! Read
}
