import akka.NotUsed
import akka.actor._
import akka.http.scaladsl._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.ws._
import akka.stream._
import akka.stream.scaladsl._
import scala.concurrent.duration._
import scala.concurrent.Await
import scala.io.StdIn
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.server.Directives._
import scala.concurrent.Future
import scala.concurrent.blocking
import auction.Messages._
import auction._



object Server extends App {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  import system.dispatcher
  val sleep = Future { 
    blocking { 
      Thread.sleep(60)
    }
    "done" 
  }
  val squad = system.actorOf(SquadListener.propsBefore)
  
  squad ! GetAuctionState
  scala.io.StdIn.readLine()
  squad ! GetAuctionState
  scala.io.StdIn.readLine()
  squad ! GetAuctionState
  scala.io.StdIn.readLine()
  squad ! GetAuctionState
  scala.io.StdIn.readLine()
  system.stop(squad)
  
  val x = system.terminate()
  Await.result(x,10.seconds)
  println("done")
  
}
