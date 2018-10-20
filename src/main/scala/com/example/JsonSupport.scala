package com.example

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val statisticsJsonFormat = jsonFormat3(StatisticsResult)
  implicit val responceJsonFormat = jsonFormat1(ApiResult)
}
