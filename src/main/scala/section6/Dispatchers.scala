package section6

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import akka.actor.Props
import java.{util => ju}
import scala.util.Random
import scala.concurrent.Future
import scala.concurrent.ExecutionContext

object Dispatchers extends App {
  class Counter extends Actor with ActorLogging {
    var count = 0
    def receive: Actor.Receive = {
      case msg =>
        count += 1
        log.info(s"[$count] ${msg.toString()}")
    }
  }

  val system = ActorSystem("routerFromConfig")

  // my-dispatcher is defined in main/resources/application.conf
  val actors =
    for (i <- 1 to 10)
      yield system.actorOf(
        Props[Counter].withDispatcher("my-dispatcher"),
        s"counter_$i"
      )

  // val r = new Random()
  // for (i <- 1 to 1000) {
  //   actors(r.nextInt(10)) ! i
  // }

// from config:
  val myDispatcherFromConfig =
    system.actorOf(Props[Counter], "my-dispatcher-from-config")

  class DBActor extends Actor with ActorLogging {
    // using this dispatcher will block all other actors to from receiving messages for 5 seconds
    // implicit val executionContext: ExecutionContext = context.dispatcher

    // to avoid long running code to block actors from getting messages, use dedicated dispatcher:
    implicit val executionContext: ExecutionContext =
      context.system.dispatchers.lookup("my-dispatcher")

    def receive: Actor.Receive = {
      case msg =>
        Future {
          Thread.sleep(5000)
          log.info(msg.toString())
        }
    }
  }

  val dbActor = system.actorOf(Props[DBActor], "dbActor")
  val nonBlockingActor = system.actorOf(Props[Counter], "nonBlockingActor")

  for (i <- 1 to 1000) {
    dbActor ! "i"
    nonBlockingActor ! "i"
  }
}
