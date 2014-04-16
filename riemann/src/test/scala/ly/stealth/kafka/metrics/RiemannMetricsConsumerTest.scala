package ly.stealth.kafka.metrics

import java.util.UUID
import com.codahale.metrics.MetricRegistry
import ly.stealth.kafka.riemann.RiemannMetricsConsumer
import org.junit.Test
import org.scalatest.junit.JUnitSuite
import java.net.InetSocketAddress
import net.benmur.riemann.client._
import RiemannClient._
import akka.actor.ActorSystem
import akka.util.Timeout
import scala.concurrent.duration.Duration
import java.util.concurrent.TimeUnit
import scala.concurrent.Await

class RiemannMetricsConsumerTestclass extends JUnitSuite {

  val zkConnection: String = "192.168.86.5:2181"
  val kafkaConnection: String = "192.168.86.10:9092"
  val riemannHost: String = "192.168.50.4"
  val riemannPort: Int = 5555
  val registry = new MetricRegistry()
  val metricName: String = "test_counter"
  val counter = registry.counter(metricName)

  @Test
  def test() {
    val testTopic = UUID.randomUUID().toString
    val groupId_1 = UUID.randomUUID().toString
    counter.inc()
    counter.inc(3)

    val producer = KafkaReporter.builder(registry, kafkaConnection, testTopic).build()
    producer.report()

    val consumer = new RiemannMetricsConsumer(riemannHost, riemannPort, "basic description", "kafka", testTopic, groupId_1, zkConnection)
    consumer.read(true)

    assertRiemann()
  }

  def assertRiemann() {
    implicit val system = ActorSystem()
    implicit val timeout = Timeout(5)
    val metricsDestination = riemannConnectAs[Reliable] to new InetSocketAddress(riemannHost, riemannPort)
    val future = (metricsDestination ask Query("tagged \"kafka\""))
    Await.ready(future, Duration.create(30, TimeUnit.SECONDS))
    val events = future.value.get.get

    assume(events.size > 0)
    assume(events.last.service.get.equals("%s [total]".format(metricName)))
    assume(events.last.metric.get.equals(counter.getCount.toDouble))
  }
}