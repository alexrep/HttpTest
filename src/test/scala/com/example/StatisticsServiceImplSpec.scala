package com.example

import akka.actor.ActorSystem
import org.scalatest.{ Matchers, WordSpec }

import scala.concurrent.Future
import org.scalatest.AsyncFlatSpec

class StatisticsServiceImplSpec extends AsyncFlatSpec {
  implicit val system: ActorSystem = ActorSystem("helloAkkaHttpServer")
  implicit val ec = system.dispatcher
  val statisticsService = new StatisticsServiceImpl(new RemoteApi {
    override def prices(ids: Seq[String]): Future[ApiResult] = Future { ApiResult(Map("1" -> 1.5, "2" -> 3)) }

    override def impressions(ids: Seq[String]): Future[ApiResult] = Future { ApiResult(Map("1" -> 2, "2" -> 3)) }
  })

  val incompleteStatisticsService = new StatisticsServiceImpl(new RemoteApi {
    override def prices(ids: Seq[String]): Future[ApiResult] = Future { ApiResult(Map("1" -> 1.5)) }

    override def impressions(ids: Seq[String]): Future[ApiResult] = Future { ApiResult(Map("1" -> 2, "2" -> 3)) }
  })

  behavior of "StatisticsServiceImpl"

  it should "correctly calculate overall statistics" in {
    // You can map assertions onto a Future, then return
    // the resulting Future[Assertion] to ScalaTest:
    statisticsService.statistics(Seq("1", "2")) map { result =>
      assert(result == Map("1" -> StatisticsResult(2, 1.5, 3), "2" -> StatisticsResult(3, 3, 9)))

    }
  }
  it should "correctly calculate statistics item" in {
    // You can map assertions onto a Future, then return
    // the resulting Future[Assertion] to ScalaTest:
    assert(statisticsService.statisticsResult(2, 4) == StatisticsResult(4, 2, 8))
  }

  it should "correctly calculate overall statistics when data are incomplete" in {
    // You can map assertions onto a Future, then return
    // the resulting Future[Assertion] to ScalaTest:
    incompleteStatisticsService.statistics(Seq("1", "2")) map { result =>
      assert(result == Map("1" -> StatisticsResult(2, 1.5, 3)))

    }
  }

}
