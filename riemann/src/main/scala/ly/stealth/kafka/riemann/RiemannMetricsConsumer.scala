package ly.stealth.kafka.riemann

import kafka.consumer.{ConsumerConfig, Consumer}
import kafka.serializer._
import java.util.Properties
import kafka.utils.Logging
import scala.collection.JavaConversions._
import ly.stealth.kafka.metrics.KafkaMetricsReport
import net.benmur.riemann.client._
import RiemannClient._
import akka.util.Timeout
import akka.actor.ActorSystem
import java.net.InetSocketAddress
import com.fasterxml.jackson.databind.ObjectMapper
import kafka.consumer.Whitelist

class RiemannMetricsConsumer(riemannHost: String,
                             riemannPort: Integer,
                             descriptionText: String,
                             tagList: String,
                             topic: String,
                             groupId: String,
                             zookeeperConnect: String,
                             zkSessionTimeoutMs: Int,
                             readFromStartOfStream: Boolean = true,
                             stateMatcher: (String, Double) => String = null,
                             defaultState: String = "info") extends Logging {
  val props = new Properties()
  props.put("group.id", groupId)
  props.put("zookeeper.connect", zookeeperConnect)
  props.put("auto.offset.reset", if (readFromStartOfStream) "smallest" else "largest")
  props.put("zookeeper.session.timeout.ms", zkSessionTimeoutMs.toString)

  val config = new ConsumerConfig(props)
  val connector = Consumer.create(config)

  val filterSpec = new Whitelist(topic)
  val mapper = new ObjectMapper();

  info("Trying to start consumer: topic=%s for zk=%s and groupId=%s".format(topic, zookeeperConnect, groupId))
  val stream = connector.createMessageStreamsByFilter(filterSpec, 1, new StringDecoder(), new StringDecoder()).get(0)
  info("Started consumer: topic=%s for zk=%s and groupId=%s".format(topic, zookeeperConnect, groupId))

  implicit val system = ActorSystem()
  implicit val timeout = Timeout(5)
  val metricsDestination = riemannConnectAs[Reliable] to new InetSocketAddress(riemannHost, riemannPort) withValues (host("host")
    | description(descriptionText) | tags(tagList))

  def read[T](onlyOnce: Boolean = false) = {
    info("reading on stream now")
    val it = stream.iterator()
    while (it.hasNext) {
      val messageAndTopic = it.next
      try {
        val report = mapper.readValue(messageAndTopic.message(), classOf[KafkaMetricsReport])
        if (report.getCounters() != null) {
          for ((name, counter) <- report.getCounters()) {
            sendMetric("%s [%s]".format(name, "total"), counter.getCount)
          }
        }

        if (report.getHistograms() != null) {
          for ((name, histogram) <- report.getHistograms()) {
            sendMetric("%s [%s]".format(name, "total"), histogram.getCount)
            sendMetric("%s [%s]".format(name, "max"), histogram.getMax)
            sendMetric("%s [%s]".format(name, "min"), histogram.getMin)
            sendMetric("%s [%s]".format(name, "mean"), histogram.getMean)
            sendMetric("%s [%s]".format(name, "P50"), histogram.getP50)
            sendMetric("%s [%s]".format(name, "P75"), histogram.getP75)
            sendMetric("%s [%s]".format(name, "P95"), histogram.getP95)
            sendMetric("%s [%s]".format(name, "P98"), histogram.getP98)
            sendMetric("%s [%s]".format(name, "P99"), histogram.getP99)
            sendMetric("%s [%s]".format(name, "P999"), histogram.getP999)
            sendMetric("%s [%s]".format(name, "standard deviation"), histogram.getStddev)
          }
        }

        if (report.getTimers() != null) {
          for ((name, timer) <- report.getTimers()) {
            sendMetric("%s [%s(%s)]".format(name, "total duration", timer.getDuration_units), timer.getCount)
            sendMetric("%s [%s(%s)]".format(name, "max duration", timer.getDuration_units), timer.getMax)
            sendMetric("%s [%s(%s)]".format(name, "min duration", timer.getDuration_units), timer.getMin)
            sendMetric("%s [%s(%s)]".format(name, "mean duration", timer.getDuration_units), timer.getMean)
            sendMetric("%s [%s(%s)]".format(name, "P50 duration", timer.getDuration_units), timer.getP50)
            sendMetric("%s [%s(%s)]".format(name, "P75 duration", timer.getDuration_units), timer.getP75)
            sendMetric("%s [%s(%s)]".format(name, "P95 duration", timer.getDuration_units), timer.getP95)
            sendMetric("%s [%s(%s)]".format(name, "P98 duration", timer.getDuration_units), timer.getP98)
            sendMetric("%s [%s(%s)]".format(name, "P99 duration", timer.getDuration_units), timer.getP99)
            sendMetric("%s [%s(%s)]".format(name, "P999 duration", timer.getDuration_units), timer.getP999)

            sendMetric("%s [%s(%s)]".format(name, "M1 rate", timer.getRate_units), timer.getM1_rate)
            sendMetric("%s [%s(%s)]".format(name, "M5 rate", timer.getRate_units), timer.getM5_rate)
            sendMetric("%s [%s(%s)]".format(name, "M15 rate", timer.getRate_units), timer.getM15_rate)
            sendMetric("%s [%s(%s)]".format(name, "mean rate", timer.getRate_units), timer.getMean_rate)

            sendMetric("%s [%s(%s)]".format(name, "standard deviation", timer.getRate_units), timer.getStddev)
          }
        }

        if (report.getMeters() != null) {
          for ((name, meter) <- report.getMeters()) {
            sendMetric("%s [%s(%s)]".format(name, "total", meter.getUnits), meter.getCount)
            sendMetric("%s [%s(%s)]".format(name, "M1 rate", meter.getUnits), meter.getM1_rate)
            sendMetric("%s [%s(%s)]".format(name, "M5 rate", meter.getUnits), meter.getM5_rate)
            sendMetric("%s [%s(%s)]".format(name, "M15 rate", meter.getUnits), meter.getM15_rate)
            sendMetric("%s [%s(%s)]".format(name, "mean rate", meter.getUnits), meter.getMean_rate)
          }
        }
      } catch {
        case e: Throwable =>
          error("Error processing message, skipping this message: ", e)
          throw e
      } finally {
        if (onlyOnce) close()
      }
    }
  }

  private def sendMetric(name: String, metricValue: Double) = {
    state(detectMetricState(name, metricValue)) | service(name) | metric(metricValue) |>> metricsDestination
  }

  private def detectMetricState(name: String, metricValue: Double): String = {
    if (stateMatcher == null) defaultState
    else stateMatcher(name, metricValue)
  }

  def close() {
    connector.shutdown()
  }
}