package com.example
import org.scalatest.{ Matchers, WordSpecLike }
import akka.http.scaladsl.model.{ ContentTypes, StatusCodes }

import scala.concurrent.duration._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.util.Timeout
import akka.pattern
import com.typesafe.config.ConfigFactory

import scala.concurrent.Future

class StatisticsRoutesSpec extends WordSpecLike with Matchers with ScalatestRouteTest with StatisticsRoutes {
  implicit val context = system.dispatcher
  override val statisticsService = new mocks.StatisticsServiceMock()
  implicit val timeout = Timeout(4.seconds)
  val configuration = ConfigFactory.load()

  "StatisticsRoutes" should {

    "return statistics results" in {
      statisticsService.setStatisticsResults(() => Future { Map("1" -> StatisticsResult(1, 1, 1)) })

      Get("/statistics?id=1") ~> statsRoutes ~> check {
        status should ===(StatusCodes.OK)
        contentType should ===(ContentTypes.`application/json`)

        entityAs[String] should ===("""{"1":{"impressions":1,"price":1.0,"spent":1.0}}""")
      }
    }

    "return a error if request takes more than defined timeout" in {
      val overTimeout = requestTimeout + 200
      statisticsService.setStatisticsResults(() =>
        pattern.after(overTimeout.millis, system.scheduler) { Future { Map("1" -> StatisticsResult(1, 1, 1)) } })

      Get("/statistics?id=1") ~> statsRoutes ~> check {
        status should ===(StatusCodes.InternalServerError)

        entityAs[String] should ===("An error occurred: Request takes too long")
      }
    }

    "validate parameters presence" in {
      statisticsService.setStatisticsResults(() => Future { Map("1" -> StatisticsResult(1, 1, 1)) })

      Get("/statistics") ~> statsRoutes ~> check {
        status should ===(StatusCodes.BadRequest)

        entityAs[String] should ===("Id parameters required")
      }
    }
  }

}
