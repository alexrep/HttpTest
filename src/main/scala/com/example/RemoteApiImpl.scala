package com.example

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import akka.pattern

import scala.concurrent.duration._
import scala.concurrent.{ Future, Promise, TimeoutException }
import com.typesafe.config._

class RemoteApiImpl(config: Config)(implicit val system: ActorSystem, implicit val materializer: ActorMaterializer) extends RemoteApi with JsonSupport {
  val http = Http()
  implicit val executionContext = system.dispatcher
  val timeout = config.getInt("app.request-timeout").millis
  val address = s"${config.getString("app.remote.host")}:${config.getString("app.remote.port")}"

  def prices(ids: Seq[String]): Future[ApiResult] = {
    makeRequest(s"http://${address}/prices?${paramsToQueryString(ids)}", ids.size)
  }

  def impressions(ids: Seq[String]): Future[ApiResult] = {
    makeRequest(s"http://${address}/impressions?${paramsToQueryString(ids)}", ids.size)
  }

  def makeRequest(url: String, entityCount: Int): Future[ApiResult] = {
    val p = Promise[ApiResult]
    makeSingleRequest(url, p, entityCount)
    system.scheduler.scheduleOnce(timeout) {
      if (!p.isCompleted) {
        p.failure(new TimeoutException("External service timeout"))
      }
    }
    p.future
  }

  def makeSingleRequest(url: String, p: Promise[ApiResult], entityCount: Int) {
    if (!p.isCompleted) {
      http.singleRequest(
        HttpRequest(uri = url)
      ).foreach({ response =>
          if (response.status.isSuccess()) {
            Unmarshal(response.entity).to[ApiResult].foreach({ res =>
              if (checkResult(res, entityCount)) {
                p.success(res)
              } else {
                makeSingleRequest(url, p, entityCount)
              }
            })
          } else {
            makeSingleRequest(url, p, entityCount)
          }
        })
    }
  }

  def paramsToQueryString(ids: Seq[String]): String = {
    ids.map({ id => s"id=$id" }).mkString("&")
  }

  def checkResult(res: ApiResult, entityCount: Int) = {
    res.results.keys.size == entityCount
  }
}
