package com.example

import scala.concurrent.{ ExecutionContext, Future }

class StatisticsServiceImpl(api: RemoteApi)(implicit ec: ExecutionContext) extends StatisticsService {
  def statistics(ids: Seq[String]): Future[Map[String, StatisticsResult]] = {
    val pricesFuture = api.prices(ids)
    val impressionsFuture = api.impressions(ids)

    for {
      prices <- pricesFuture
      impressions <- impressionsFuture
    } yield countStatistics(prices.results, impressions.results)

  }

  def countStatistics(prices: Map[String, Double], impressions: Map[String, Double]): Map[String, StatisticsResult] = {
    prices.foldLeft(Map.empty[String, StatisticsResult]) {
      (acc, kv) =>
        {
          val id = kv._1
          val price = kv._2
          if (impressions.contains(id)) {
            acc + (id -> statisticsResult(price, impressions(id)))
          } else {
            acc
          }
        }
    }
  }

  def statisticsResult(price: Double, impressions: Double): StatisticsResult = {
    StatisticsResult(impressions.toInt, price, price * impressions)
  }

}
