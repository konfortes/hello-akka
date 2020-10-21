package actorssection3

import akka.actor.Actor
import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.ActorRef

object ActorCapabilities extends App {
  val actorSystem = ActorSystem("mySystem")
  class SimpleActor extends Actor {
    override def receive: Receive = {
      case msg: String =>
        println(s"[${context.self.path}] hey, I received $msg")
      case num: Int =>
        println(s"[${context.self.path}] I don't support integers")
      case SayHiTo(ref) =>
        ref ! println(s"[${context.self.path}]hello from $self")
    }
  }

  val simpleActor = actorSystem.actorOf(Props[SimpleActor], "simpleActor")

  simpleActor ! "hello"
  simpleActor ! 42

  case class SayHiTo(ref: ActorRef)

  val alice = actorSystem.actorOf(Props[SimpleActor], "Alice")
  val bob = actorSystem.actorOf(Props[SimpleActor], "Bob")

  alice ! SayHiTo(bob)
}
