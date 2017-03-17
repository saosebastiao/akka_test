package auction

import java.time.OffsetDateTime



object Domain {

  
  // domain objects
  final case class Squad(id: Int, filters: Option[Int] = None)
  final case class Party(id: Int, filters: Option[Int] = None)
  final case class SuccessfulBid(squad: Int, party: Int, price: Int)
  final case class Price(value: Int, currency: String = "USD")
  
  final case class AuctionConfig(startTime: OffsetDateTime,interval: Int, priceStep: Int, priceStart: Int) {
    val entryFreeze = startTime.minusHours(1)
    val auctionStop = {
      var p = priceStart
      var time = startTime
      do {
        p -= priceStep
        time = time.plusMinutes(interval)
      } while (p > 0)
      time
    }
  }


  // events
  final case object GetParticipants
  final case object FreezeEntry
  final case object FreezeFilters
  final case object StartAuction
  final case object PriceTimeout
  final case object EndAuction

  sealed trait AuctionState 
  final case object PreAuction extends AuctionState
  final case class EntryFreeze(effective: OffsetDateTime) extends AuctionState
  final case class FiltersFreeze(effective: OffsetDateTime) extends AuctionState
  final case class ActiveAuction(price: Price,effective: OffsetDateTime) extends AuctionState
  final case object PostAuction extends AuctionState
  
  // messages 
  final case object GetAuctionState
  final case class Message(value: String)
  final case class Bid(partyID: Int,squadID: Int)
  final case class StateUpdate(from: AuctionState, to: AuctionState)
  final case object AuctionLeave
  final case object AuctionListen
  final case object ListenConfirm
  final case object LeaveConfirm
}
