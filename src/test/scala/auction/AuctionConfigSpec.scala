package auction


import java.time.OffsetDateTime
import java.time.LocalDate
import org.scalatest._
import Domain._
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.Duration

class AuctionConfigSpec extends WordSpecLike with Matchers {
 
  
  "An AuctionConfig" must {

    "have a correct freeze time" in {
      val date = LocalDate.now()
      val time = LocalTime.of(14,0,0)
      val start = OffsetDateTime.of(date, time, ZoneOffset.ofHours(0))
      val config = AuctionConfig(start,Duration.ofMinutes(1),1,10)
      assert(config.startTime.isAfter(config.entryFreeze))
    }
    "init to correct state when before entry freeze" in {
      val start = OffsetDateTime.now().plusMinutes(65).withNano(0).withSecond(0)
      val config = AuctionConfig(start,Duration.ofMinutes(1),1,3)
      val iter = config.iterator()
      var e = iter.next()
      assert(e._1.isBefore(config.entryFreeze))
      e._2 shouldBe GetParticipants
      e = iter.next()
      assert(e._1.isBefore(config.entryFreeze))
      e._2 shouldBe GetParticipants
      e = iter.next()
      assert(e._1.isEqual(config.entryFreeze))
      e._2 shouldBe FreezeEntry
      e = iter.next()
      assert(e._1.isEqual(config.startTime))
      e._2 shouldBe StartAuction(3)
      e = iter.next()
      assert(e._1.isEqual(config.startTime.plus(config.interval)))
      e._2 shouldBe PriceChange(2)
      e = iter.next()
      assert(e._1.isEqual(config.startTime.plus(config.interval.multipliedBy(2))))
      e._2 shouldBe PriceChange(1)
      e = iter.next()
      assert(e._1.isEqual(config.startTime.plus(config.interval.multipliedBy(3))))
      e._2 shouldBe EndAuction
      iter.hasNext shouldBe(false)
    }
    "init to correct state when after entry freeze" in {
      val start = OffsetDateTime.now().plusMinutes(30).withNano(0).withSecond(0)
      val config = AuctionConfig(start,Duration.ofMinutes(1),1,3)
      val iter = config.iterator()
      var e = iter.next()
      assert(e._1.isAfter(config.entryFreeze))
      e._2 shouldBe GetParticipants
      e = iter.next()
      assert(e._1.isEqual(config.entryFreeze))
      e._2 shouldBe FreezeEntry
      e = iter.next()
      assert(e._1.isEqual(config.startTime))
      e._2 shouldBe StartAuction(3)
      e = iter.next()
      assert(e._1.isEqual(config.startTime.plus(config.interval)))
      e._2 shouldBe PriceChange(2)
      e = iter.next()
      assert(e._1.isEqual(config.startTime.plus(config.interval.multipliedBy(2))))
      e._2 shouldBe PriceChange(1)
      e = iter.next()
      assert(e._1.isEqual(config.startTime.plus(config.interval.multipliedBy(3))))
      e._2 shouldBe EndAuction
      iter.hasNext shouldBe(false)
    }
    "init to correct state when after auction start" in {
      val start = OffsetDateTime.now().minusMinutes(30).withNano(0).withSecond(0)
      val config = AuctionConfig(start,Duration.ofMinutes(1),1,3)
      config.iterator().foreach(println)
      val iter = config.iterator()
      var e = iter.next()
      assert(e._1.isAfter(config.startTime))
      e._2 shouldBe GetParticipants
      e = iter.next()
      assert(e._1.isEqual(config.entryFreeze))
      e._2 shouldBe FreezeEntry
      e = iter.next()
      assert(e._1.isEqual(config.startTime))
      e._2 shouldBe StartAuction(3)
      e = iter.next()
      assert(e._1.isEqual(config.startTime.plus(config.interval)))
      e._2 shouldBe PriceChange(2)
      e = iter.next()
      assert(e._1.isEqual(config.startTime.plus(config.interval.multipliedBy(2))))
      e._2 shouldBe PriceChange(1)
      e = iter.next()
      assert(e._1.isEqual(config.startTime.plus(config.interval.multipliedBy(3))))
      e._2 shouldBe EndAuction
      iter.hasNext shouldBe(false)
    }
  }
}
