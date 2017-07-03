package auction

import java.time.OffsetDateTime
import java.time.Duration




object Domain {

  final case class SquadFilters()
  final case class PartyFilters()
  
  
  // domain objects
  final case class Squad(id: Int, filters: Option[Int] = None, winningBid: Option[SuccessfulBid] = None)
  final case class Party(id: Int, filters: Option[Int] = None)
  
  final case class SuccessfulBid(squad: Int, party: Int, price: Price)
  
  
  final case class Price(value: Int, currency: String = "USD"){
    def dropPrice(step: Int) = this.copy(value = Math.max(value - step,0))
  }
  
  final case class AuctionConfig(startTime: OffsetDateTime,interval: Duration, priceStep: Int, priceStart: Price) {
    val entryFreeze = startTime.minusHours(1)
    val updateInterval = Duration.ofMinutes(3)
    def iterator() = new Iterator[AuctionState]{
      var cursor: Option[AuctionState] = None
      private def calcNext(): AuctionState = cursor match {
          case None => PreAuction(OffsetDateTime.now())
          case Some(PreAuction(t)) if t.plus(updateInterval).isBefore(entryFreeze) => 
              PreAuction(t.plus(updateInterval))
          case Some(PreAuction(_)) => EntryFreeze(entryFreeze)
          case Some(EntryFreeze(_)) => ActiveAuction(priceStart,startTime)
          case Some(ActiveAuction(p,t)) if p.dropPrice(priceStep).value > 0 => 
            ActiveAuction(priceStart.dropPrice(priceStep),t.plus(interval))
          case Some(ActiveAuction(_,t)) => PostAuction(t.plus(interval))
          case Some(PostAuction(t)) => PostAuction(t)
      }
      def hasNext = cursor match {
        case Some(PostAuction(_)) => false
        case _ => true
      }
      def next() = {
        cursor = Some(calcNext())
        cursor.get
      }
    }
  }
  
  

  
  sealed trait UserMessage
  sealed trait ClientMessage
  sealed trait AuctionMessage
  
  // messages 
  final case object GetAuctionState extends UserMessage
  final case class Bid(partyID: Int,squadID: Int) extends UserMessage
  final case class UpdateParty(partyID: Int) extends UserMessage
  final case class UpdateSquad(squadID: Int) extends UserMessage
  final case class CurrentState(state: AuctionState, squads: Seq[Squad], parties: Seq[Party]) extends AuctionMessage
  
  final case object AuctionLeave
  final case object AuctionListen
  final case object ListenConfirm
  final case object LeaveConfirm

  // states
  sealed trait AuctionState {
    def effective: OffsetDateTime
  }
  
  final case class PreAuction(effective: OffsetDateTime) extends AuctionState
  final case class EntryFreeze(effective: OffsetDateTime) extends AuctionState
  final case class ActiveAuction(price: Price, effective: OffsetDateTime) extends AuctionState
  final case class PostAuction(effective: OffsetDateTime) extends AuctionState
  

}
