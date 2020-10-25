package section6

import akka.actor.ActorSystem
import akka.actor.Actor
import akka.actor.ActorLogging
import com.typesafe.config.Config
import akka.dispatch.UnboundedPriorityMailbox
import akka.dispatch.PriorityGenerator
import akka.actor.Props
import akka.dispatch.ControlMessage
import com.typesafe.config.ConfigFactory

object Mailboxes extends App {
  val system =
    ActorSystem("mySystem", ConfigFactory.load().getConfig("mailboxesDemo"))

  class SimpleActor extends Actor with ActorLogging {
    def receive: Actor.Receive = {
      case msg => log.info(msg.toString())
    }
  }

  // custom priority mailbox
  class SupportTicketPriorityMailbox(
      settings: ActorSystem.Settings,
      config: Config
  ) extends UnboundedPriorityMailbox(PriorityGenerator {
        case msg: String if msg.startsWith("[P0]") => 0
        case msg: String if msg.startsWith("[P1]") => 1
        case msg: String if msg.startsWith("[P2]") => 2
        case msg: String if msg.startsWith("[P3]") => 3
        case _                                     => 4
      })

  val supportTicketLogger =
    system.actorOf(
      Props[SimpleActor].withDispatcher("support-ticket-dispatcher")
    )

  supportTicketLogger ! "[P3] nice to have"
  supportTicketLogger ! "[P0] URGENT!"
  supportTicketLogger ! "[P1] when you have time"

  // prioritize time before executing can not be controlled

  // control-aware-mailbox:
  // mark important messages:
  case object ManagementTicket extends ControlMessage

  // control-mailbox is defined in application.conf
  val controlAwareActor =
    system.actorOf(Props[SimpleActor].withMailbox("control-mailbox"))

  controlAwareActor ! "[P3] nice to have"
  controlAwareActor ! "[P0] URGENT!"
  controlAwareActor ! ManagementTicket // this should be handled first but for some reason it isn't

  // alternatively - can be defined in akka.actor.deployment:
  val altControlAwareActor =
    system.actorOf(Props[SimpleActor], "altControlAwareActor")
}
