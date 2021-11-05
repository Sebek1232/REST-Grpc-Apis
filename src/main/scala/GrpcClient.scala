
import com.example.protos.awsApi.{AwsReply, InvokeAwsApiGrpc, LogParameters}
import com.typesafe.config.ConfigFactory
import io.grpc.ManagedChannelBuilder

import java.util.logging.Logger


//Client that makes grpc calls to the server
object GrpcClient {
  def main(args: Array[String]): Unit = {
    val logger = Logger.getLogger(classOf[GrpcClient].getName)

    val config = ConfigFactory.parseResources("app.conf")
      .resolve();


    val channel = ManagedChannelBuilder.forAddress("localhost", 50051).usePlaintext().build
    //Pass parameters to server
    val request = LogParameters(time = config.getString("testPara.time1"),
                                interval = config.getString("testPara.interval1"))
    val blockingStub = InvokeAwsApiGrpc.blockingStub(channel)
    //server reply
    val reply: AwsReply = blockingStub.awsResponse(request)
    logger.info("Server Replied With: ")
    logger.info(reply.toString)
  }

}
class GrpcClient

