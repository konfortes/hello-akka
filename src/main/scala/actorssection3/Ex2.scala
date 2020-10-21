package actorssection3

import akka.actor.ActorSystem
import akka.actor.ActorRef
import akka.actor.AbstractActor.Receive
import akka.actor.Actor
import akka.actor.Props

object Ex2 extends App {
  val system = ActorSystem("mySystem")

  object BankAccount {
    case class Withdraw(amount: Int, ref: ActorRef = ActorRef.noSender)
    case class Deposit(amount: Int, ref: ActorRef = ActorRef.noSender)
    case class Statement(ref: ActorRef = ActorRef.noSender)

    case class Failure(f: String)
  }

  class BankAccount extends Actor {
    var funds = 1000
    override def receive: Receive = {
      case BankAccount.Deposit(amount, _) =>
        funds += amount
      case BankAccount.Withdraw(amount, _) =>
        if (amount > 5000) {
          sender() ! BankAccount.Failure("can't withdraw more than 5000")
        } else {
          funds -= amount
        }
      case BankAccount.Statement(_) => sender() ! funds
    }
  }

  class ATM extends Actor {
    override def receive: Actor.Receive = {
      case BankAccount.Deposit(amount, ref) =>
        ref ! BankAccount.Deposit(amount)
      case BankAccount.Withdraw(amount, ref) =>
        ref ! BankAccount.Withdraw(amount)
      case BankAccount.Statement(ref) => ref ! BankAccount.Statement()
      case funds: Int                 => println(s"bank account ${sender()} has $funds funds")
      case BankAccount.Failure(f)     => println(s"failed: $f")
    }
  }

  val bankAccount = system.actorOf(Props[BankAccount], "bankAccount")
  val atm = system.actorOf(Props[ATM], "ATM")

  atm ! BankAccount.Deposit(500, bankAccount)
  atm ! BankAccount.Deposit(1500, bankAccount)
  atm ! BankAccount.Withdraw(1000, bankAccount)

  atm ! BankAccount.Statement(bankAccount)

  atm ! BankAccount.Withdraw(10000, bankAccount)

}
