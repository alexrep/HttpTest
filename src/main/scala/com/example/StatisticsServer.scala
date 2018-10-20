package com.example

//#quick-start-server
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import akka.actor.{ ActorRef, ActorSystem }
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.typesafe.config.{ Config, ConfigFactory }

//#main-class
object StatisticsServer extends App with StatisticsRoutes {
  val configuration = ConfigFactory.load()
  implicit val system: ActorSystem = ActorSystem("helloAkkaHttpServer")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val api = new RemoteApiImpl(configuration)

  val statisticsService = new StatisticsServiceImpl(api)

  lazy val routes: Route = statsRoutes
  Http().bindAndHandle(routes, "localhost", 8080)

  println(s"Server online at http://localhost:8080/")

  Await.result(system.whenTerminated, Duration.Inf)
}

