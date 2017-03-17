package auction


import java.time.OffsetDateTime
import java.time.LocalDate
import org.scalatest._
import Domain._
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset

class AuctionConfigSpec extends WordSpecLike with Matchers {
 
  
  "An AuctionConfig" must {
    val date = LocalDate.now()
    val time = LocalTime.of(14,0,0)
    val start = OffsetDateTime.of(date, time, ZoneOffset.ofHours(0))
    println(start)
    val config = AuctionConfig(start,1,1,10)
    
    "have a correct freeze time" in {
      assert(config.startTime.isAfter(config.entryFreeze))
    }
    "calculate correct end of auction" in {
      assert(config.auctionStop.minusMinutes(10).isEqual(config.startTime))
    }

  }
}
