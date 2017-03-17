package auction

import java.time.OffsetDateTime
import java.time.Duration




object Domain {

  
  // domain objects
  final case class Squad(id: Int, filters: Option[Int] = None)
  final case class Party(id: Int, filters: Option[Int] = None)
  final case class SuccessfulBid(squad: Int, party: Int, price: Int)
  final case class Price(value: Int, currency: String = "USD")
  
  final case class AuctionConfig(startTime: OffsetDateTime,interval: Duration, priceStep: Int, priceStart: Int) {
    val entryFreeze = startTime.minusHours(1)
    val updateInterval = Duration.ofMinutes(3)
    def iterator() = new Iterator[(OffsetDateTime,StateEvent)]{
      var cursor: (OffsetDateTime,StateEvent) = (OffsetDateTime.now(),Init)
      private def calcNext: (OffsetDateTime,StateEvent) = cursor match {
          case (t,Init) => (t,GetParticipants)
          case (t,GetParticipants) 
            if t.plus(updateInterval).isBefore(entryFreeze) => (t.plus(updateInterval),GetParticipants)
          case (t,GetParticipants) => (entryFreeze,FreezeEntry)
          case (t,FreezeEntry) => (startTime,StartAuction(priceStart))
          case (t,StartAuction(p)) => (t.plus(interval),PriceChange(p - priceStep))
          case (t,PriceChange(p)) if (p - priceStep) > 0 => (t.plus(interval),PriceChange(p - priceStep))
          case (t,PriceChange(_)) => (t.plus(interval),EndAuction)
          case (t,EndAuction) => (t,EndAuction)
      }
      def hasNext = cursor match {
        case (_,EndAuction) => false
        case _ => true
      }
      def next() = {
        cursor = calcNext
        cursor
      }
    }
  }
  
  
  // messages 
  final case object GetAuctionState
  final case class Message(value: String)
  final case class Bid(partyID: Int,squadID: Int)
  final case class StateUpdate(from: AuctionState, to: AuctionState)
  final case object AuctionLeave
  final case object AuctionListen
  final case object ListenConfirm
  final case object LeaveConfirm

  // events
  sealed trait StateEvent
  final case object Init extends StateEvent
  final case object GetParticipants extends StateEvent
  final case object FreezeEntry extends StateEvent
  final case object FreezeFilters extends StateEvent
  final case class StartAuction(price: Int) extends StateEvent
  final case class PriceChange(price: Int) extends StateEvent
  final case object EndAuction extends StateEvent
  
  sealed trait AuctionState 
  final case object PreAuction extends AuctionState
  final case class EntryFreeze(effective: OffsetDateTime) extends AuctionState
  final case class FiltersFreeze(effective: OffsetDateTime) extends AuctionState
  final case class ActiveAuction(price: Price,effective: OffsetDateTime) extends AuctionState
  final case object PostAuction extends AuctionState
  

}
