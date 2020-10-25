package section7
import org.scalatest.{WordSpecLike, BeforeAndAfterAll}
import akka.testkit.{TestKit, ImplicitSender}
import akka.actor.{Actor, ActorSystem, ActorLogging, Props}
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import scala.util.Success
import scala.util.Failure
import org.scalatest.BeforeAndAfterEach
import akka.actor.ActorRef
import akka.pattern.pipe

class AskSpec
    extends TestKit(ActorSystem("AskSpec"))
    with ImplicitSender
    with WordSpecLike
    with BeforeAndAfterAll
    with BeforeAndAfterEach {
  override def afterAll(): Unit = TestKit.shutdownActorSystem(system)

  import AskSpec._
  import AuthManger._

  var authManager: ActorRef = ActorRef.noSender

  override protected def beforeEach(): Unit =
    authManager = system.actorOf(Props[AuthManager])

  "an AuthManager" should {
    "return not found when no such user" in {
      authManager ! Authenticate("john_doe", "1234")

      expectMsg(AuthFailed(AUTH_FAILURE_NOT_FOUND))
    }

    "return incorrect password when password is incorrect" in {
      authManager ! RegisterUser("john_doe", "1234")
      authManager ! Authenticate("john_doe", "4321")

      expectMsg(AuthFailed(AUTH_FAILURE_PASSWORD_INCORRECT))
    }

    "authenticates successfully on correct password" in {
      authManager ! RegisterUser("john_doe", "1234")
      authManager ! Authenticate("john_doe", "1234")

      expectMsg(AuthSucceeded)
    }
  }

}

object AskSpec {
  case class Read(Key: String)
  case class Write(Key: String, value: String)
  class KVActor extends Actor with ActorLogging {
    def receive: Receive = online(Map())

    def online(kv: Map[String, String]): Receive = {
      case Read(key) =>
        log.info(s"reading $key")
        sender() ! kv.get(key)
      case Write(key, value) =>
        log.info(s"writing the value $value into key $key")
        context.become(online(kv + (key -> value)))
    }
  }

  case class RegisterUser(username: String, password: String)
  case class Authenticate(username: String, password: String)
  case class AuthFailed(reason: String)
  case object AuthSucceeded
  object AuthManger {
    val AUTH_FAILURE_NOT_FOUND = "username not found"
    val AUTH_FAILURE_PASSWORD_INCORRECT = "incorrect password"
    val AUTH_FAILURE_SYSTEM_ERROR = "system error"
  }
  class AuthManager extends Actor with ActorLogging {
    import AuthManger._

    implicit val timeout: Timeout = Timeout(1 second)
    implicit val executionContext: ExecutionContext = context.dispatcher

    protected val authDB = context.actorOf(Props[KVActor])
    def receive: Actor.Receive = {
      case RegisterUser(username, password) =>
        log.info(s"registering user $username with password $password")
        authDB ! Write(username, password)
      case Authenticate(username, password) =>
        handleAuthentication(username, password)
    }

    def handleAuthentication(username: String, password: String) = {
      // sender() returns the last sender. since the future might be scheduled on another thread, another sender can send message in the meantime
      // therefore we keep the reference of the original sender
      // this way we don't close over a mutable state (sender())
      val originalSender = sender()
      log.info(
        s"trying to authenticate user $username with password $password"
      )
      val future = authDB ? Read(username)
      future.onComplete {
        case Success(None) =>
          originalSender ! AuthFailed(AUTH_FAILURE_NOT_FOUND)
        case Success(Some(dbPassword: String)) =>
          if (dbPassword == password) originalSender ! AuthSucceeded
          else originalSender ! AuthFailed(AUTH_FAILURE_PASSWORD_INCORRECT)
        case Failure(_) =>
          originalSender ! AuthFailed(AUTH_FAILURE_SYSTEM_ERROR)
      }
    }
  }

  // doesnt expose you to onComplete callbacks which breaks actor encapsulation
  class PipedAuthManager extends AuthManager {
    import AuthManger._

    override def handleAuthentication(username: String, password: String) = {
      val future = authDB ? Read(username)
      val passwordFuture = future.mapTo[Option[String]]
      val responseFuture = passwordFuture.map {
        case None => AuthFailed(AUTH_FAILURE_NOT_FOUND)
        case Some(dbPassword) =>
          if (dbPassword == passwordFuture) AuthSucceeded
          else AuthFailed(AUTH_FAILURE_PASSWORD_INCORRECT)
      } // Future[Any] - will be completed with the response sent back

      responseFuture.pipeTo(sender())
    }
  }
}
