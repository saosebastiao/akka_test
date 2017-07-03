package auction

import auction.Domain._
import java.time.OffsetDateTime
import java.time.Duration

class DAO {
  private val auctionConfigs = Map[Int,AuctionConfig](
    (1,AuctionConfig(OffsetDateTime.now().withNano(0).plusMinutes(6).plusSeconds(1),Duration.ofSeconds(1),1,Price(3))),
    (2,AuctionConfig(OffsetDateTime.now().withNano(0).plusSeconds(2),Duration.ofSeconds(1),1,Price(3))),
    (3,AuctionConfig(OffsetDateTime.now().withNano(0).minusSeconds(120),Duration.ofSeconds(1),1,Price(3)))
  )
  private var squads = Map[Int,Squad](1 -> Squad(1),2 -> Squad(2), 3->Squad(3))
  private var parties = Map[Int,Party](1 -> Party(1),2 -> Party(2), 3->Party(3))
  private var transactions = Set[SuccessfulBid]()
  
  def getAuction(auctionID: Int) = {
    auctionConfigs.get(auctionID).get
  }
  def getSquad(squadID: Int) = {
    squads.get(squadID).get
  }
  def getSquads = {
    squads
  }
  def getAvailableSquads = {
    val taken = transactions.map(_.squad)
    squads.seq.filter(x => taken.contains(x._1)).toMap
  }
  def getParty(partyID: Int) = {
    parties.get(partyID).get
  }
  def getParties = {
    parties
  }
  def recordTransaction(squadID: Int, partyID: Int, price: Price) = {
    val bid = SuccessfulBid(squadID,partyID,price)
    transactions += bid
    bid
  }
  def getTransactions = {
    transactions
  }
}