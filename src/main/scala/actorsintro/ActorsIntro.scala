package actorsintro

import akka.actor.ActorSystem
import akka.actor.Actor
import akka.actor.Props

object ActorsIntro extends App {
  // part1 - actors system
  val actorSystem = ActorSystem("firstActorSystem")

  // part2 - create actors
  class WordCountActor extends Actor {
    var totalWords = 0
    // def receive: PartialFunction[Any, Unit] = {
    def receive: Receive = {
      case message: String =>
        println(s"[wordCounter] I have received a message: $message")
        totalWords += message.split(" ").length
      case msg => println(s"Can't handle ${msg.toString()}")
    }
  }

  // part3 - instantiate
  val wordCounter = actorSystem.actorOf(Props[WordCountActor], "wordCounter")
  val anotherWordCounter =
    actorSystem.actorOf(Props[WordCountActor], "anotherWordCounter")

  // part4 - communicate
  // wordCounter.! "Hello"
  wordCounter ! "Hello there, how do you do?"
  anotherWordCounter ! "howdey"
}
