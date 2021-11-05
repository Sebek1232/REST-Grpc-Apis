
import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.server.Directives.pathPrefix
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.http.scaladsl.server.Directives._

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object RestServer extends App with LambdaController {

  implicit val actorSystem = ActorSystem("AkkaHTTP")
  implicit val materializer = ActorMaterializer()

  lazy val apiRoutes: Route = pathPrefix("api") {
    routes
  }
  //binds to port
  Http().bindAndHandle(apiRoutes, config.getString("server.host"), config.getInt("server.port"))
  //awaits requests from clients
  logger.info("Server Awaiting Requests")
  Await.result(actorSystem.whenTerminated, Duration.Inf)
}
class RestServer
