import GrpcServer.logger
import com.example.protos.awsApi.{AwsReply, InvokeAwsApiGrpc, LogParameters}
import com.typesafe.config.ConfigFactory

import java.net.{HttpURLConnection, URL}
import java.util.logging.Logger
import io.grpc.{Server, ServerBuilder}
import org.apache.http.HttpHeaders

import scala.concurrent.{ExecutionContext, Future}
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.{ContentType, StringEntity}
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.util.EntityUtils
import spray.json.JsValue


/**
 * [[https://github.com/grpc/grpc-java/blob/v0.15.0/examples/src/main/java/io/grpc/examples/helloworld/HelloWorldServer.java]]
 */


object GrpcServer {
  private val logger = Logger.getLogger(classOf[GrpcServer].getName)
  val config = ConfigFactory.parseResources("app.conf")
    .resolve();

  //Start grpc server
  def main(args: Array[String]): Unit = {
    val server = new GrpcServer(ExecutionContext.global)
    server.start()
    server.blockUntilShutdown()
  }

  private val port = config.getInt("server.port")
}

class GrpcServer(executionContext: ExecutionContext) { self =>
  private[this] var server: Server = null
  val config = ConfigFactory.parseResources("app.conf")
    .resolve();

  private def start(): Unit = {
    server = ServerBuilder.forPort(GrpcServer.port).addService(InvokeAwsApiGrpc.bindService(new InvokeAwsApiImpl, executionContext)).build.start
    GrpcServer.logger.info("Server started, listening on " + GrpcServer.port)
    sys.addShutdownHook {
      GrpcServer.logger.severe("*** shutting down gRPC server since JVM is shutting down")
      self.stop()
      GrpcServer.logger.severe("*** server shut down")
    }
  }

  private def stop(): Unit = {
    if (server != null) {
      server.shutdown()
    }
  }

  private def blockUntilShutdown(): Unit = {
    if (server != null) {
      server.awaitTermination()
    }
  }

  private class InvokeAwsApiImpl extends InvokeAwsApiGrpc.InvokeAwsApi {
    override def awsResponse(req: LogParameters): Future[AwsReply] = {
      //Reply back to client with aws message
      val reply = AwsReply(message = findLogs(req.time, req.interval))
      Future.successful(reply)
    }

    //calls awsLambda to see if start time of logs exist in file
    def doesTimeExist(time:String): Boolean =
    {

      val config = ConfigFactory.parseResources("app.conf")
        .resolve();
      val post = new HttpPost(config.getString("api.doesTimeExistUrl"))
      post.setHeader(HttpHeaders.AUTHORIZATION, config.getString("api.doesTimeExistAuth"))
      post.setEntity(new StringEntity("{" + time + "}",ContentType.APPLICATION_JSON))
      val client = new DefaultHttpClient
      val response = client.execute(post)
      GrpcServer.logger.info("Calling awsLambda: DoesTimeExist")
      val responseEntity = response.getEntity()
      val strReply = EntityUtils.toString(responseEntity);
      if(strReply == "1")
        true
      else
        false
    }

    //after verifying if the time exists, this function calls another awsLambda function
    //which will return a all the logs in the given interval
    def findLogs(time:String, interval:String): String = {
      if(doesTimeExist(time)) {
        val post = new HttpPost(config.getString("api.findLogsUrl"))
        post.setHeader(HttpHeaders.AUTHORIZATION, config.getString("api.findLogsAuth"))
        val message = "{" + time + "," + interval + "}"
        post.setEntity(new StringEntity(message, ContentType.APPLICATION_JSON))
        val client = new DefaultHttpClient
        GrpcServer.logger.info("Calling awsLambda: findLogs")
        val response = client.execute(post)
        val responseEntity = response.getEntity()
        val str = EntityUtils.toString(responseEntity)
        str;
      }
      else
          throw new Throwable("No Log Found")
    }
  }
}

