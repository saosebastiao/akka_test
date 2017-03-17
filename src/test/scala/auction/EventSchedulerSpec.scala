package auction

import akka.actor.ActorSystem
import akka.actor.Actor
import akka.actor.Props
import akka.testkit.{ TestActors, TestKit, ImplicitSender }
import org.scalatest._
import Domain._
import akka.testkit.TestActorRef
import scala.concurrent.duration._
import akka.actor.ActorRef


class EventSchedulerSpec extends TestKit(ActorSystem("MySpec")) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {
 
  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  
  "An EventScheduler actor" must {
    val scheduler = TestActorRef(new Actor {
      import context.dispatcher
      var parent: Option[ActorRef] = None
      def receive = {
        case n: Int => {
          parent = Some(sender)
          context.system.scheduler.scheduleOnce(n.seconds,self,if(n > 0) s"due: $n" else s"past due: $n")
        }
        case s: String => parent.get ! s
      }
    })
    "send all overdue messages immediately" in {
      scheduler ! -1
      expectMsg(1.seconds,"past due: -1")
      scheduler ! -3
      expectMsg(1.seconds,"past due: -3")
      scheduler ! -20
      expectMsg(1.seconds,"past due: -20")
    }
    "send all due messages in proper time" in {
      scheduler ! 1
      expectMsg(2.seconds,"due: 1")
      scheduler ! 3
      expectMsg(4.seconds,"due: 3")
      scheduler ! 5
      expectMsg(6.seconds,"due: 5")
    }
  }

}
