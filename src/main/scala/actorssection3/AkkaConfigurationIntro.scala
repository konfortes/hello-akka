package actorssection3

import com.typesafe.config.ConfigFactory
import akka.actor.ActorSystem
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.Props

object AkkaConfigurationIntro extends App {
  val configString = """
    | akka {
    |   loglevel = "ERROR"
    | }
    """.stripMargin
  val config = ConfigFactory.parseString(configString)
  val system = ActorSystem("mySystem", ConfigFactory.load(config))

  class SimpleLoggingActor extends Actor with ActorLogging {
    def receive: Actor.Receive = {
      case message => log.info(s"got this message: $message")
    }
  }

  val actor = system.actorOf(Props[SimpleLoggingActor], "simpleLoggingActor")

  actor ! "some message"

  val anotherSystem = ActorSystem("anotherSystem")
  val anotherActor =
    anotherSystem.actorOf(Props[SimpleLoggingActor], "anotherActor")

  anotherActor ! "another message"
}
