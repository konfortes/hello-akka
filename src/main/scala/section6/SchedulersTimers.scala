package section6

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorSystem
import akka.actor.Props
import scala.concurrent.duration._
import java.time.LocalDateTime
import akka.actor.Cancellable
import akka.actor.Timers

object SchedulersTimers extends App {
  val system = ActorSystem("mySystem")
  class SimpleActor extends Actor with ActorLogging {
    def receive: Receive = {
      case msg => log.info(msg.toString())
    }
  }

  val simpleActor = system.actorOf(Props[SimpleActor], "simpleActor")
  system.log.info("scheduling reminder for simpleActor")

//   implicit val dispatcher = system.dispatcher
  import system.dispatcher

  system.scheduler.scheduleOnce(2 second) {
    simpleActor ! "reminder"
  }

  val routine = system.scheduler.schedule(5 second, 1 second) {
    simpleActor ! "scheduled task"
  }

  // cancel a schedule
  system.scheduler.scheduleOnce(10 second) {
    routine.cancel()
  }

  object SelfClosingActor {
    case object Timeout
  }
  class SelfClosingActor extends Actor with ActorLogging {
    import SelfClosingActor._
    var cancelTimeout = createTimeWindow(1 second)
    def createTimeWindow(duration: FiniteDuration): Cancellable = {
      context.system.scheduler.scheduleOnce(duration) {
        self ! Timeout
      }
    }

    def receive: Actor.Receive = {
      case Timeout => context.stop(self)
      case msg =>
        cancelTimeout.cancel()
        log.info(s"Got message $msg")
        cancelTimeout = createTimeWindow(1 second)
    }
  }

  val oneSecondActor =
    system.actorOf(Props[SelfClosingActor], "oneSecondActor")

  val cancel = system.scheduler.schedule(0 second, 800 milli) {
    oneSecondActor ! "message!"
  }

  system.scheduler.scheduleOnce(2 seconds) {
    cancel.cancel()
  }

  case object TimerKey
  case object Start
  case object Reminder
  case object Stop
  class TimerBasedSelfClosingActor extends Actor with ActorLogging with Timers {
    timers.startSingleTimer(TimerKey, Start, 500 millisecond)
    def receive: Actor.Receive = {
      case Start =>
        log.info("bootstraping")
        timers.startPeriodicTimer(TimerKey, Reminder, 1 second)
      case Reminder => log.info("I'm alive")
      case Stop =>
        log.warning("Stopping")
        timers.cancel(TimerKey)
        context.stop(self)
    }
  }

  val timerBasedSelfClosingActor = system.actorOf(
    Props[TimerBasedSelfClosingActor],
    "timerBasedSelfClosingActor"
  )

  system.scheduler.scheduleOnce(5 seconds) {
    timerBasedSelfClosingActor ! Stop
  }
}
