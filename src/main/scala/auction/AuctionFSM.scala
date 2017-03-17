package auction

import akka.actor.Actor
import akka.actor.Props
import akka.actor.ActorRef
import akka.actor.ActorSystem
import scala.concurrent.duration._
import java.time.OffsetDateTime
import java.time.Duration
import scala.concurrent.Await
import scala.concurrent.duration._
import akka.actor.FSM
import Domain._
import scala.concurrent.Future
import akka.actor.Cancellable


object Helpers {
  import scala.language.implicitConversions
  protected implicit class Synchronizer[T](future: Future[T]){
    def sync = Await.result(future,10.seconds)
  }
}

object FilterResolver {
  
}




trait ListenerManager { this: Actor =>
  private var listeners = Set[ActorRef]()
  def broadcast[T](msg: T) = {
    listeners.foreach(s => s ! msg)
  }
  protected def SubscribeWatcher: Receive = {
    case AuctionListen => {
      sender ! ListenConfirm
      listeners += sender
    }
    case AuctionLeave => {
      sender ! LeaveConfirm
      listeners -= sender
      if(listeners.isEmpty){
        context.stop(self)
      }
    }
  }
}


trait EventScheduler { this: Actor =>
  import context.dispatcher
  private var cancellables = Set[Cancellable]()
  private def scheduleMsg(msg: Any,time: OffsetDateTime) = {
    val delay = Duration.between(OffsetDateTime.now(),time).getSeconds.seconds
    cancellables += context.system.scheduler.scheduleOnce(delay, self, msg)
  }
  def scheduleRecurringMsg(msg: Any,every: Duration) = {
    val delay = every.getSeconds.seconds
    cancellables += context.system.scheduler.schedule(delay,delay,self,msg)
  }
  def clearScheduledMsgs = {
    cancellables.foreach(c => c.cancel)
    cancellables = Set()
  }
  def toDuration(time: OffsetDateTime) = Duration.between(OffsetDateTime.now(),time).getSeconds.seconds


}

class AuctionFSM(auctionID: Int, model: DAO) extends Actor with ListenerManager with EventScheduler {
  import Helpers._
  
  val config: AuctionConfig = model.getAuction(auctionID)
  var availableSquads: Map[Int,Squad] = model.getAvailableSquads
  var parties: Map[Int,Party] = model.getParties
  var transactions = model.getTransactions
  def receive = SubscribeWatcher.orElse {
    case x => sender ! x
  }
  def PreAuction: Receive = {
    scheduleRecurringMsg(GetParticipants,Duration.ofMinutes(3))
    return SubscribeWatcher.orElse {
      case GetParticipants => {
        availableSquads = model.getSquads
        parties = model.getParties
        broadcast(Message(s"squads: $availableSquads, parties: $parties"))
      }
    }
  }
  
  def EntryFreeze: Receive = {
    return SubscribeWatcher.orElse {
      case _ =>
    }
  }
  def FiltersFreeze: Receive = {
    return SubscribeWatcher.orElse {
      case _ =>
    }
  }
  def ActiveAuction(price: Price): Receive = {
    return SubscribeWatcher.orElse {
      case _ =>
    }
  }
  def PostAuction: Receive = {
    return SubscribeWatcher.orElse {
      case _ =>
    }
  }
  override def postStop(){
    AuctionFSM.dropAuction(auctionID)
  }
  def init = {
    val now = OffsetDateTime.now()
    if(now.isBefore(config.entryFreeze)){
      PreAuction
    } else if(now.isAfter(config.entryFreeze) && now.isBefore(config.startTime)){
      EntryFreeze
    } else {
      var price = config.priceStart
      var time = config.startTime
      while(price > 0 && now.isAfter(time)){
        price -= price
        time = time.plusMinutes(config.interval)
      }
      if(price > 0){
        ActiveAuction(Price(price))
      } else {
        PostAuction
      }
    }
  }

}

object AuctionFSM {
  
  @volatile private var auctions = Map[Int,ActorRef]()
  private val dao = new DAO
  def props(auctionID: Int) = Props(new AuctionFSM(auctionID,dao))

  def getAuction(auctionID: Int)(implicit system: ActorSystem) = {
    val actorTest = auctions.get(auctionID)
    actorTest match {
      case Some(actor) => actor
      case None => {
        val spawned = system.actorOf(props(auctionID))
        auctions += (auctionID -> spawned)
        spawned
      }
    }
  }
  private def dropAuction(auctionID: Int) = {
    auctions -= auctionID
  }
}

