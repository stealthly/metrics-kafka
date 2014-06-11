package ly.stealth.kafka.metrics

import java.util.UUID
import ly.stealth.kafka.riemann.PsutilMetricsConsumer
import akka.actor.ActorSystem
import akka.util.Timeout
import net.benmur.riemann.client.RiemannClient._
import java.net.InetSocketAddress
import net.benmur.riemann.client.Query
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import org.specs2.mutable.Specification
import kafka.utils.Logging
import java.util.concurrent.TimeUnit

class PsutilMetricsAndRiemannSpec extends Specification with Logging {
  val zkConnection: String = "localhost:2181"
  val kafkaConnection: String = "localhost:9092"
  val riemannHost: String = "localhost"
  val riemannPort: Int = 5555

  val psutilTopic = "psutil-kafka-topic"

  "PsutilsMetricsConsumer" should {
    "be able to transfer metrics from Kafka topic to Riemann" in {
      val groupId = UUID.randomUUID().toString
      var success = true
      try {
        val consumer = new PsutilMetricsConsumer(riemannHost, riemannPort, "basic description", "kafka", psutilTopic, groupId, zkConnection, 30000)
        consumer.transfer(true)
      } catch {
        case e: Exception => success = false
      }

      success must beTrue
    }
  }

  "Riemann users" should {
    "be able to query metrics, written with PsutilsMetricsConsumer, from Riemann" in {
      implicit val system = ActorSystem()
      implicit val timeout = Timeout(30, TimeUnit.SECONDS)

      TimeUnit.SECONDS.sleep(5)

      val metricsDestination = riemannConnectAs[Reliable] to new InetSocketAddress(riemannHost, riemannPort)
      val cpuFuture = metricsDestination ask Query("tagged \"cpu\"")
      val memoryFuture = metricsDestination ask Query("tagged \"memory\"")
      val diskFuture = metricsDestination ask Query("tagged \"disk\"")

      Await.ready(cpuFuture, Duration.Inf)
      Await.ready(memoryFuture, Duration.Inf)
      Await.ready(diskFuture, Duration.Inf)

      val cpuEvents = cpuFuture.value.get.get
      val memoryEvents = memoryFuture.value.get.get
      val diskEvents = diskFuture.value.get.get

      cpuEvents must not(beEmpty)
      memoryEvents must not(beEmpty)
      diskEvents must not(beEmpty)
    }
  }

}
