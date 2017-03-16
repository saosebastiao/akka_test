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
import Messages._
import Domain._
import Events._
import scala.concurrent.Future
import akka.actor.Cancellable


object Helpers {
  import scala.language.implicitConversions
  protected implicit def toDuration(time: OffsetDateTime) = Duration.between(OffsetDateTime.now(),time).getSeconds.seconds
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
      listeners += sender
    }
    case AuctionLeave => {
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
  def scheduleMsg(msg: Any,time: OffsetDateTime) = {
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
}

class AuctionFSM(auctionID: Int, model: DAO) extends Actor with ListenerManager with EventScheduler {
  import Helpers._
  
  val config: AuctionConfig = model.getAuction(auctionID)
  var availableSquads: Map[Int,Squad] = model.getAvailableSquads
  var parties: Map[Int,Party] = model.getParties
  var transactions = model.getTransactions
  def receive = ???
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
}

object AuctionFSM {
  
  @volatile private var auctions = Map[Int,ActorRef]()
  private val dao = new DAO
  private def props(auctionID: Int) = Props(new AuctionFSM(auctionID,dao))

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
  def dropAuction(auctionID: Int) = {
    auctions -= auctionID
  }
}

