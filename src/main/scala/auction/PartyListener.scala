package auction

import akka.actor.Actor
import Domain._
import akka.actor.Props
import akka.actor.actorRef2Scala


class PartyListener(auctionID: Int) extends Actor {

  val auction = AuctionFSM.getAuction(auctionID)(context.system)
  override def preStart(){
    auction ! AuctionListen
  }
  override def postStop(){
    auction ! AuctionLeave
  }
  def receive = {
    case x: Bid => auction ! x
    case GetAuctionState => auction ! GetAuctionState
    case x => println(s"listener received $x")
  }
}
object PartyListener {
  def propsBefore = Props(classOf[PartyListener],1)
  def propsDuring = Props(classOf[PartyListener],2)
  def propsAfter = Props(classOf[PartyListener],3)
  
}