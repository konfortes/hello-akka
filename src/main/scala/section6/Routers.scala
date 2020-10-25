package section6

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.Props
import akka.routing.{Router, RoundRobinRoutingLogic}
import akka.routing.ActorRefRoutee
import akka.actor.Terminated
import akka.actor.ActorSystem
import akka.routing.RoundRobinPool
import com.typesafe.config.ConfigFactory
import akka.routing.FromConfig
import akka.io.Udp.SO.Broadcast

object Routers extends App {

  class Master extends Actor with ActorLogging {
    private val workers = for (i <- 1 to 5) yield {
      val worker = context.actorOf(Props[Worker], "worker" + i)
      context.watch(worker)

      ActorRefRoutee(worker)
    }
    private val router = Router(RoundRobinRoutingLogic(), workers)

    def receive: Receive = {
      case Terminated(ref) =>
        router.removeRoutee(ref)
        val newWorker = context.actorOf(Props[Worker])
        context.watch(newWorker)
        router.addRoutee(newWorker)
      case msg => router.route(msg, sender())
    }
  }

  class Worker extends Actor with ActorLogging {
    def receive: Actor.Receive = {
      case msg => log.info(msg.toString())
    }
  }

  val system = ActorSystem("mySystem")
  val master = system.actorOf(Props[Master])

//   for (i <- 1 to 10) master ! s"hello[$i]"

  // Easier way to define routing master actor
  val poolMaster =
    system.actorOf(RoundRobinPool(5).props(Props[Worker]), "poolMaster")
//   for (i <- 1 to 10) poolMaster ! s"hello[$i]"

  // can also be defined in configuration
  val configString = """
    | akka {
    |   actor.deployment{
    |     /poolMasterFromConfig{
    |       router = round-robin-pool
    |       nr-of-instances = 5
    |     }
    |   }
    | }
    """.stripMargin
  val config = ConfigFactory.parseString(configString)
  val system2 = ActorSystem("routerFromConfig", ConfigFactory.load(config))
  val poolMasterFromConfig =
    system2.actorOf(FromConfig.props(Props[Worker]), "poolMasterFromConfig")
  for (i <- 1 to 10) poolMasterFromConfig ! s"hello[$i]"

  // Broadcast message is sent to all routees
  poolMasterFromConfig ! akka.routing.Broadcast("hello to everyone")

}
