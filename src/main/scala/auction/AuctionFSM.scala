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


trait StateManager { this: Actor =>
  import context.dispatcher
  private var cancellables = Set[Cancellable]()
  protected def scheduleMsg(msg: Any,time: OffsetDateTime) = {
    val delay = Duration.between(OffsetDateTime.now(),time).getSeconds.seconds
    cancellables += context.system.scheduler.scheduleOnce(delay, self, msg)
  }
  protected def clearScheduledMsgs() = {
    cancellables.foreach(c => c.cancel)
    cancellables = Set()
  }
}

class AuctionFSM(auctionID: Int, model: DAO) extends Actor with ListenerManager with StateManager {
  import Helpers._
  
  val config: AuctionConfig = model.getAuction(auctionID)
  var availableSquads: Map[Int,Squad] = model.getAvailableSquads
  var parties: Map[Int,Party] = model.getParties
  var transactions = model.getTransactions
  def receive = SubscribeWatcher.orElse {
    case x => sender ! x
  }
  def PreAuction: Receive = {
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

