package auction

import akka.actor.ActorSystem
import akka.actor.Actor
import akka.actor.Props
import akka.testkit.{ TestActors, TestKit, ImplicitSender }
import org.scalatest._
import Domain._
import akka.testkit.TestActorRef


class AuctionFSMSpec() extends TestKit(ActorSystem("MySpec")) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {
 
  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  
  "An AuctionFSM actor" must {
    val auction = TestActorRef(AuctionFSM.props(1))
    val impl = auction.underlying
    "start up" in {
      awaitCond(!impl.isTerminated)
    }
    "register" in {
      auction ! AuctionListen
      expectMsg(ListenConfirm)
    }

    "unregister" in {
      auction ! AuctionLeave
      expectMsg(LeaveConfirm)

    }
    "shutdown" in {
      awaitCond(impl.isTerminated)
    }
  }

}
