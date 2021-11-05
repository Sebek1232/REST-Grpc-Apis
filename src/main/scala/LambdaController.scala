
import LambdaController.{doesLogExist, findLogs}
import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import com.typesafe.config.ConfigFactory
import org.apache.http.HttpHeaders
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.{ContentType, StringEntity}
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.util.EntityUtils

import java.util.logging.Logger
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success}


//acts as server and client. Server for the RESTful API and client of aws api find
//checks and find logs invoking aws lambda functions
object LambdaController {
  val logger = Logger.getLogger(classOf[LambdaController].getName)
  val config = ConfigFactory.parseResources("app.conf")
    .resolve();

  //checks if logs exist based on time
  def doesLogExist(para:String): Boolean =
  {
    val time = para.substring(0, para.indexOf(",")) + "}";
    val post = new HttpPost(config.getString("api.doesTimeExistUrl"))
    post.setHeader(HttpHeaders.AUTHORIZATION, config.getString("api.doesTimeExistAuth"))
    post.setEntity(new StringEntity(time,ContentType.APPLICATION_JSON))
    val client = new DefaultHttpClient
    val response = client.execute(post)
    logger.info("Calling awsLambda: DoesTimeExist")
    val responseEntity = response.getEntity()
    val strReply = EntityUtils.toString(responseEntity);

    if(strReply == "1")
      return true
    else
      return false
  }

  //calls doesLogExist and if it exists it returns the logs in the given interval
  def findLogs(para: String): Future[String] =
  {
    if(doesLogExist(para)) {
      val post = new HttpPost(config.getString("api.findLogsUrl"))
      post.setHeader(HttpHeaders.AUTHORIZATION, config.getString("api.findLogsAuth"))
      post.setEntity(new StringEntity(para, ContentType.APPLICATION_JSON))
      val client = new DefaultHttpClient
      logger.info("Calling awsLambda: findLogs")
      val response = client.execute(post)
      val responseEntity = response.getEntity()
      val str = EntityUtils.toString(responseEntity, "UTF-8")
      Future{
        str
      }
    }
    else
      Future
      {
        throw new Throwable("No Log Found")
      }

  }
}

trait LambdaController
  {
  implicit def actorSystem: ActorSystem

    lazy val logger = Logging(actorSystem, classOf[LambdaController])
    val config = ConfigFactory.parseResources("app.conf")
      .resolve();

    //Routes api requests and calls functions that make requests to AWS API
    lazy val routes: Route = pathPrefix("logs") {
      get {
        path(Segment) { para  =>
          onComplete(findLogs(para)) {
            _ match {
              case Success(found) =>
                logger.info("Received Logs for input: " + para)
                complete(StatusCodes.OK, found)
              case Failure(throwable) =>
                logger.error("Logs does not exist for input: " + para)
                complete(StatusCodes.InternalServerError, "Logs does not exist for input: " + para)
            }
          }
        }
      }
      post {
        path("post") {
          entity(as[String]) { para =>
            println(para);
            onComplete(findLogs(para)) {
              _ match {
                case Success(found) =>
                  logger.info("Received Logs for input: " + para)
                  complete(StatusCodes.OK, found)
                case Failure(throwable) =>
                  logger.error("Logs does not exist for input: " + para)
                  complete(StatusCodes.InternalServerError, "Logs does not exist for input: " + para)
              }
            }
          }
        }
      }
  }
}



