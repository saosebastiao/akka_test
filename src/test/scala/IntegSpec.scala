//
//import akka.actor.ActorSystem
//import akka.actor.Actor
//import akka.actor.Props
//import akka.testkit.{ TestActors, TestKit, ImplicitSender }
//import org.scalatest._
//
//class IntegSpec() extends TestKit(ActorSystem("MySpec")) with ImplicitSender
//  with WordSpecLike with Matchers with BeforeAndAfterAll {
// 
//  override def afterAll {
//    TestKit.shutdownActorSystem(system)
//  }
// 
//  "An Echo actor" must {
// 
//    "send back messages unchanged" in {
//      val echo = system.actorOf(TestActors.echoActorProps)
//      echo ! "hello world"
//      expectMsg("hello world")
//    }
//  }
//}

