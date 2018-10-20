package com.example

import java.nio.charset.Charset

import akka.actor.ActorSystem

import scala.concurrent.duration._
import akka.http.scaladsl.server.Directives._

import scala.util.{ Failure, Success }
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.get
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import com.typesafe.config._

import scala.concurrent.{ Future, TimeoutException }
import akka.pattern.after

trait StatisticsRoutes extends JsonSupport {
  def statisticsService: StatisticsService
  def configuration: Config

  implicit def system: ActorSystem
  implicit def executionContext = system.dispatcher

  def parseParameters(queryString: String): Seq[String] = {
    queryString.split("&").map { element => element.split("=").lastOption.getOrElse("") }.filter { id => id.length > 0 }
  }

  def wrapWithTimeout[T](future: Future[T], timeout: FiniteDuration) = {
    val t = after(duration = timeout, using = system.scheduler)(Future.failed(new TimeoutException("Request takes too long")))
    Future firstCompletedOf Seq(future, t)
  }

  def requestTimeout = configuration.getInt("app.request-timeout")

  lazy val statsRoutes: Route =
    get {
      path("statistics") {
        extract(ctx => ctx.request.uri.queryString(charset = Charset.defaultCharset)) {
          case None => complete(HttpResponse(BadRequest, entity = "Id parameters required"))
          case Some(params) => onComplete(wrapWithTimeout(statisticsService.statistics(parseParameters(params)), requestTimeout.millis)) {
            case Success(value) => complete(value)
            case Failure(ex) => complete(HttpResponse(InternalServerError, entity = s"An error occurred: ${ex.getMessage}"))
          }
        }

      }
    }
}
