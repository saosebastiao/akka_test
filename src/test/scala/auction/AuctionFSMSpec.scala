package auction

import akka.actor.ActorSystem
import akka.actor.Actor
import akka.actor.Props
import akka.testkit.{ TestActors, TestKit, ImplicitSender }
import org.scalatest._
import Messages._
import Events._


class AuctionFSMSpec() extends TestKit(ActorSystem("MySpec")) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {
 
  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }
  val auction = AuctionFSM.getAuction(1)
 
  "An AuctionFSM actor" must {
 
    "send back messages unchanged" in {
      auction ! "hello world"
      expectMsg("hello world")
    }
  }
}
