package section5

import akka.actor.ActorSystem
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.PoisonPill
import akka.actor.Terminated

object StartingStoppingActors extends App {
  val system = ActorSystem("mySystem")

  object Parent {
    case class StartChild(name: String)
    case class StopChild(name: String)
    case object Stop
  }

  class Parent extends Actor with ActorLogging {
    import Parent._
    def receive: Actor.Receive = withChildren(Map())

    def withChildren(children: Map[String, ActorRef]): Receive = {
      case StartChild(name) =>
        log.info(s"starting new child $name")
        val childRef = context.actorOf(Props[Child], "child-" + name)
        context.become(withChildren(children + (name -> childRef)))
      case StopChild(name) =>
        log.info(s"stopping child $name")
        val childRefOption = children.get(s"child-$name")
        childRefOption.foreach(context.stop(_))
      case Stop => context.stop(self) // stops also all child actors
    }
  }

  class Child extends Actor with ActorLogging {
    def receive: Actor.Receive = {
      case msg => log.info(msg.toString())
    }
  }

  import Parent._

  val parent = system.actorOf(Props[Parent], "parent")
  parent ! StartChild("yaya")
  val childRef = system.actorSelection("/user/parent/child-yaya")

  childRef ! "hi kid"

  parent ! Stop // will stop all children first, following by stopping itself

  val poisonChild = system.actorOf(Props[Child], "poisonChild")
  poisonChild ! "hello there"

  poisonChild ! PoisonPill // stops the actor. can use also Kill (will cause the actor to throw exception)

  class Watcher extends Actor with ActorLogging {
    import Parent._
    def receive: Actor.Receive = {
      case StartChild(name) =>
        log.info(s"I'm about to watch $name")
        val childRef = context.actorOf(Props[Child], name)
        context.watch(childRef) // can use also unwatch
      case Terminated(actorRef) =>
        log.info(s"${actorRef.path} was just terminated")
    }
  }

  val watcher = system.actorOf(Props[Watcher], "watcher")
  watcher ! StartChild("watchedChild")
  val watchedChild = system.actorSelection("/user/watcher/watchedChild")
  Thread.sleep(500)
  watchedChild ! PoisonPill

}
