import LambdaController.{doesLogExist, findLogs}
import com.typesafe.config.ConfigFactory
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class apiTests extends AnyFlatSpec with Matchers {
  val config = ConfigFactory.parseResources("app.conf")
    .resolve();

  it should "obtain if log2 exists" in {
    doesLogExist("{" + config.getString("testPara.time2") + "," + config.getString("testPara.interval2") + "}") shouldBe false
  }

  it should "obtain if log1 exists" in {
    doesLogExist("{" + config.getString("testPara.time1") + "," + config.getString("testPara.interval1") + "}") shouldBe true
  }

  it should "obtain if log3 exists" in {
    doesLogExist("{" + config.getString("testPara.time3") + "," + config.getString("testPara.interval3") + "}") shouldBe false
  }

  it should "obtain if log4 exists" in {
    doesLogExist("{" + config.getString("testPara.time4") + "," + config.getString("testPara.interval4") + "}") shouldBe false
  }

  it should "obtain if log5 exists" in {
    doesLogExist("{" + config.getString("testPara.time5") + "," + config.getString("testPara.interval5") + "}") shouldBe true
  }




}