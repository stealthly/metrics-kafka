package ly.stealth.kafka.core.metrics

import com.yammer.metrics.core._
import com.yammer.metrics.stats.Snapshot
import java.util.concurrent.TimeUnit
import com.yammer.metrics.reporting.AbstractPollingReporter
import kafka.producer.{KeyedMessage, ProducerConfig, Producer}
import java.util

class TopicReporter(metricsRegistry: MetricsRegistry,
                    producerConfig: ProducerConfig,
                    prefix: String,
                    predicate: MetricPredicate = MetricPredicate.ALL,
                    clock: Clock = Clock.defaultClock()) extends AbstractPollingReporter(metricsRegistry, "kafka-topic-reporter") with MetricProcessor[Context] {
  private val producerMap: util.Map[MetricName, Producer[AnyRef, AnyRef]] = new util.HashMap[MetricName, Producer[AnyRef, AnyRef]]
  private var startTime: Long = 0L

  def run() {
    val metrics: util.Set[util.Map.Entry[MetricName, Metric]] = getMetricsRegistry.allMetrics.entrySet
    try {
      import scala.collection.JavaConversions._
      for (entry <- metrics) {
        val metricName: MetricName = entry.getKey
        val metric: Metric = entry.getValue
        if (predicate.matches(metricName, metric)) {
          val context: Context = new Context {
            def getProducer(header: String): Producer[AnyRef, AnyRef] = {
              val producer: Producer[AnyRef, AnyRef] = getActualProducer(metricName, header)
              producer
            }
          }
          metric.processWith(this, entry.getKey, context)
        }
      }
    }
    catch {
      case e: Exception => {
        e.printStackTrace
      }
    }
  }

  def processMeter(name: MetricName, meter: Metered, context: Context) {
    val header: String = "# time,count,1 min rate,mean rate,5 min rate,15 min rate"
    val producer: Producer[AnyRef, AnyRef] = context.getProducer(header)
    val topic = "%s-metrics-meter".format(prefix)
    val message = new StringBuilder().append(meter.count).append(',').append(meter.oneMinuteRate).append(',').append(meter.meanRate).append(',').append(meter.fiveMinuteRate).append(',').append(meter.fifteenMinuteRate).toString
    send(producer, header, topic, message)
  }

  def processCounter(name: MetricName, counter: Counter, context: Context) {
    val header: String = "# time,count"
    val producer: Producer[AnyRef, AnyRef] = context.getProducer(header)
    val topic = "%s-metrics-counter".format(prefix)
    val message = counter.count.toString
    send(producer, header, topic, message)
  }

  def processHistogram(name: MetricName, histogram: Histogram, context: Context) {
    val header: String = "# time,min,max,mean,median,stddev,95%,99%,99.9%"
    val producer: Producer[AnyRef, AnyRef] = context.getProducer(header)
    val snapshot: Snapshot = histogram.getSnapshot
    val topic = "%s-metrics-histogram".format(prefix)
    val message = new StringBuilder().append(histogram.min).append(',').append(histogram.max).append(',').append(histogram.mean).append(',').append(snapshot.getMedian).append(',').append(histogram.stdDev).append(',').append(snapshot.get95thPercentile).append(',').append(snapshot.get99thPercentile).append(',').append(snapshot.get999thPercentile).toString
    send(producer, header, topic, message)
  }

  def processTimer(name: MetricName, timer: Timer, context: Context) {
    val header: String = "# time,min,max,mean,median,stddev,95%,99%,99.9%"
    val producer: Producer[AnyRef, AnyRef] = context.getProducer(header)
    val snapshot: Snapshot = timer.getSnapshot
    val topic = "%s-metrics-timer".format(prefix)
    val message = new StringBuilder().append(timer.min).append(',').append(timer.max).append(',').append(timer.mean).append(',').append(snapshot.getMedian).append(',').append(timer.stdDev).append(',').append(snapshot.get95thPercentile).append(',').append(snapshot.get99thPercentile).append(',').append(snapshot.get999thPercentile).toString
    send(producer, header, topic, message)
  }

  def processGauge(name: MetricName, gauge: Gauge[_], context: Context) {
    val header: String = "# time,value"
    val producer: Producer[AnyRef, AnyRef] = context.getProducer(header)
    val topic = "%s-metrics-gauge".format(prefix)
    val message = gauge.value.toString
    send(producer, header, topic, message)
  }

  override def start(period: Long, unit: TimeUnit) {
    this.startTime = clock.time
    super.start(period, unit)
  }

  override def shutdown() {
    try {
      super.shutdown()
    }
    finally {
      import scala.collection.JavaConversions._
      for (producer <- producerMap.values) {
        producer.close()
      }
    }
  }

  private def getActualProducer(metricName: MetricName, header: String): Producer[AnyRef, AnyRef] = {
    var producer: Producer[AnyRef, AnyRef] = null
    producerMap synchronized {
      producer = producerMap.get(metricName)
      if (producer == null) {
        producer = new Producer(producerConfig)
        producerMap.put(metricName, producer)
      }
    }
    producer
  }

  private def send(producer: Producer[AnyRef, AnyRef], header: String, topic: String, message: String) = {
    val time: Long = TimeUnit.MILLISECONDS.toSeconds(clock.time - startTime)
    producer.send(new KeyedMessage(topic, "%s\n%d,%s".format(header, time, message).getBytes("UTF8")))
  }
}

trait Context {
  def getProducer(header: String): Producer[AnyRef, AnyRef]
}