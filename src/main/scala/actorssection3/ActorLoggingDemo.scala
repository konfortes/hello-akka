package actorssection3

import akka.actor.ActorSystem
import akka.actor.Actor
import akka.event.Logging
import akka.actor.Props
import akka.actor.ActorLogging

object ActorLoggingDemo extends App {
  val system = ActorSystem("mySystem")

  class SimpleActorWithExplicitLogger extends Actor {
    val logger = Logging(context.system, this)
    def receive: Actor.Receive = {
      case msg => logger.info(msg.toString())
    }
  }

  class ActorWithLogging extends Actor with ActorLogging {
    val logger = Logging(context.system, this)
    def receive: Actor.Receive = {
      case (a, b) => log.info("two values: {} and {}", a, b)
      case msg    => log.info(msg.toString())
    }
  }

  val simpleActorWithExplicitLogger = system.actorOf(
    Props[SimpleActorWithExplicitLogger],
    "simpleActorWithExplicitLogger"
  )
  val actorWithLogging = system.actorOf(
    Props[ActorWithLogging],
    "actorWithLogging"
  )

  simpleActorWithExplicitLogger ! "hey"
  actorWithLogging ! "hello"
  actorWithLogging ! (42, 73)
}
