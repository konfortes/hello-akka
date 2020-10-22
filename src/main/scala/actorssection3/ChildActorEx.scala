package actorssection3

import akka.actor.ActorSystem
import akka.actor.Actor
import akka.actor.Props
import akka.actor.ActorRef

object ChildActorEx extends App {
  val system = ActorSystem("mySystem")

  object WordCounterMaster {
    case class Initialize(nChildren: Int)
    case class WordCountTask(text: String)
    case class WordCountReply(count: Int)
  }
  class WordCounterMaster extends Actor {
    import WordCounterMaster._

    def receive: Actor.Receive = {
      case Initialize(num) =>
        val workerRefs =
          //   (1 to num).map(n =>
          //     context.actorOf(Props[WordCounterWorker], "worker" + n)
          //   )
          for (n <- 1 to num)
            yield context.actorOf(Props[WordCounterWorker], "worker" + n)
        context.become(withWorkers(workerRefs))
    }

    def withWorkers(
        workers: Seq[ActorRef],
        currentWorkerIndex: Int = 0
    ): Receive = {
      case WordCountTask(text) =>
        val nextWorkerIndex = (currentWorkerIndex + 1) % workers.length
        workers(nextWorkerIndex) ! text
        context.become(withWorkers(workers, nextWorkerIndex))
      case WordCountReply(count) =>
        println(s"got count of $count from ${sender()}")
    }
  }

  class WordCounterWorker extends Actor {
    import WordCounterMaster._
    def receive: Actor.Receive = {
      case text: String =>
        sender() ! WordCountReply(text.split(" ").length)
    }
  }

  import WordCounterMaster._

  val wcm = system.actorOf(Props[WordCounterMaster], "master")
  wcm ! Initialize(5)

  (1 to 7).foreach(_ =>
    wcm ! WordCountTask("pleas count how many words are in this text")
  )
}
