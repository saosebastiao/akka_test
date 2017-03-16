package auction
import akka.actor.Actor
import Messages._
import akka.actor.Props
import akka.actor.actorRef2Scala

sealed trait TestCase
final case object Before extends TestCase
final case object During extends TestCase
final case object After extends TestCase

class SquadListener(auctionID: Int) extends Actor {

  val auction = AuctionFSM.getAuction(auctionID)(context.system)
  override def preStart(){
    auction ! AuctionListen
  }
  override def postStop(){
    auction ! AuctionLeave
  }
  def receive = {
    case GetAuctionState => auction ! GetAuctionState
    case x => println(s"listener received $x")
  }
}
object SquadListener {
  def propsBefore = Props(classOf[SquadListener],1)
  def propsDuring = Props(classOf[SquadListener],2)
  def propsAfter = Props(classOf[SquadListener],3)
  
}