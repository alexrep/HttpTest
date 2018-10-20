package com.example

import scala.concurrent.Future

trait StatisticsService {
  def statistics(ids: Seq[String]): Future[Map[String, StatisticsResult]]
}
