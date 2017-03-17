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
import akka.actor.PoisonPill


class StateManagerSpec extends TestKit(ActorSystem("MySpec")) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {
 
  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  
  "A StateManager actor" must {
    def stateManager(auctionID: Int) = TestActorRef(new Actor with StateManager {
      import context.dispatcher
      val model = new DAO
      val config: AuctionConfig = model.getAuction(auctionID)
      config.iterator().foreach({
        case (t,e) => scheduleMsg(e,t)
      })
      def receive = {
        case e => {
          testActor ! e
        }
      }
      override def postStop() = {
        clearScheduledMsgs()
      }
    })

    "transition to FreezeEntry" in {
      val a = stateManager(1)
      expectMsg(GetParticipants)
      expectMsg(1.seconds,FreezeEntry)
      a ! PoisonPill
    }
    "transition to StartAuction" in {
      val b = stateManager(2)
      expectMsg(GetParticipants)
      expectMsg(1.seconds,FreezeEntry)
      expectMsg(3.seconds,StartAuction(3))
      b ! PoisonPill
    }
    "transition to PriceChange" in {
      val c = stateManager(2)
      expectMsg(GetParticipants)
      expectMsg(1.seconds,FreezeEntry)
      expectMsg(3.seconds,StartAuction(3))
      expectMsg(3.seconds,PriceChange(2))
      expectMsg(3.seconds,PriceChange(1))
      expectMsg(3.seconds,EndAuction)
      c ! PoisonPill
    }
    "transition to AuctionEnd" in {
      val c = stateManager(3)
      expectMsg(GetParticipants)
      expectMsg(1.seconds,FreezeEntry)
      expectMsg(1.seconds,StartAuction(3))
      expectMsg(1.seconds,PriceChange(2))
      expectMsg(1.seconds,PriceChange(1))
      expectMsg(1.seconds,EndAuction)
      c ! PoisonPill
    }
  }

}
