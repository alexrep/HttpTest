package com.example
import scala.concurrent.Future

trait RemoteApi {
  def prices(ids: Seq[String]): Future[ApiResult]
  def impressions(ids: Seq[String]): Future[ApiResult]
}
