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


trait StateManager { this: Actor =>

  import context.dispatcher

  private var listeners = Set[ActorRef]()
  private var cancellables = Set[Cancellable]()

  
  protected def broadcast[T](msg: T) = {
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
  protected def scheduleMsg(msg: Any,time: OffsetDateTime) = {
    val delay = Duration.between(OffsetDateTime.now(),time).getSeconds.seconds
    cancellables += context.system.scheduler.scheduleOnce(delay, self, msg)
  }
  protected def clearScheduledMsgs() = {
    cancellables.foreach(c => c.cancel)
    cancellables = Set()
  }
}

final class AuctionFSM(auctionID: Int, model: DAO) extends Actor {
  import Helpers._
  import context.dispatcher

  private var listeners = Set[ActorRef]()
  private var cancellables = Set[Cancellable]()
  private val config: AuctionConfig = model.getAuction(auctionID)
  private var availableSquads: Map[Int,Squad] = model.getAvailableSquads
  private var parties: Map[Int,Party] = model.getParties
  private var transactions = model.getTransactions
  private var currentState: Option[AuctionState] = None
  private def broadcast[T](msg: T) = {
    listeners.foreach(s => s ! msg)
  }
  
  // Initialize
  config.iterator().foreach({
    case event if event.effective.isAfter(OffsetDateTime.now()) => {
      val delay = Duration.between(OffsetDateTime.now(),event.effective).getSeconds.seconds
      cancellables += context.system.scheduler.scheduleOnce(delay, self, event)
    }
    case expired => currentState = Some(expired)
  })
  
  def receive = {
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
    case state: AuctionState =>  {
      availableSquads = model.getAvailableSquads
      parties = model.getParties
      currentState = Some(state)
    }
    case msg: UserMessage => currentState.get match {
      case p: PreAuction => msg match {
        case GetAuctionState => sender ! CurrentState(p,availableSquads.values.toSeq,parties.values.toSeq)
        case _ => throw new RuntimeException("no bids allowed before auction")
      }
      case e: EntryFreeze => {
        
      }
      case a: ActiveAuction =>{
        
      }
      case p: PostAuction => {
        
      }
    }
  }
  override def postStop(){
    cancellables.foreach(c => c.cancel)
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

