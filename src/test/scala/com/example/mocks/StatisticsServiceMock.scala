package com.example.mocks
import com.example.StatisticsResult

import scala.concurrent.{ ExecutionContext, Future }

class StatisticsServiceMock(implicit ec: ExecutionContext) extends com.example.StatisticsService {
  private var result: () => Future[Map[String, StatisticsResult]] = () => Future { Map.empty[String, StatisticsResult] }
  def setStatisticsResults(res: () => Future[Map[String, StatisticsResult]]): Unit = {
    result = res
  }
  override def statistics(ids: Seq[String]): Future[Map[String, StatisticsResult]] = result()

}
