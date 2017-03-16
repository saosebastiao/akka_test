package auction

import akka.actor.ActorSystem
import akka.actor.Actor
import akka.actor.Props
import akka.testkit.{ TestActors, TestKit, ImplicitSender }
import org.scalatest._
import Messages._
import Events._


class SquadListenerSpec() extends TestKit(ActorSystem("MySpec")) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {
 
  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }
 
  "A SquadListener actor" must {
    val echo = system.actorOf(SquadListener.propsBefore)
 
    "send back messages unchanged" in {
      echo ! "hello world"
      expectMsg("hello world")
    }
  }
}
