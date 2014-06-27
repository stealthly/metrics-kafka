package ly.stealth.kafka.core.metrics

import kafka.utils.{VerifiableProperties, Logging}
import com.yammer.metrics.Metrics
import java.util.concurrent.TimeUnit
import kafka.producer.{ProducerConfig}
import kafka.metrics.{KafkaMetricsConfig, KafkaMetricsReporter, KafkaMetricsReporterMBean}

private trait KafkaTopicReporterMBean extends KafkaMetricsReporterMBean


private class KafkaBrokerReporter extends KafkaMetricsReporter
                                              with KafkaTopicReporterMBean
                                              with Logging {
  private var underlying: TopicReporter = null
  private var props: VerifiableProperties = null
  private var running = false
  private var initialized = false


  override def getMBeanName = "kafka:type=ly.stealth.kafka.core.metrics.KafkaBrokerReporter"


  override def init(props: VerifiableProperties) {
    synchronized {
                   if (!initialized) {
                     this.props = props
                     props.props.put("metadata.broker.list", "%s:%d".format("localhost", props.getInt("port")))
                     val metricsConfig = new KafkaMetricsConfig(props)

                     this.underlying = new TopicReporter(Metrics.defaultRegistry(), new ProducerConfig(props.props), "broker%s".format(props.getString("broker.id")))
                     initialized = true
                     startReporter(metricsConfig.pollingIntervalSecs)
                   }
                 }
  }


  override def startReporter(pollingPeriodSecs: Long) {
    synchronized {
                   if (initialized && !running) {
                     underlying.start(pollingPeriodSecs, TimeUnit.SECONDS)
                     running = true
                     info("Started Kafka Topic metrics reporter with polling period %d seconds".format(pollingPeriodSecs))
                   }
                 }
  }


  override def stopReporter() {
    synchronized {
                   if (initialized && running) {
                     underlying.shutdown()
                     running = false
                     info("Stopped Kafka Topic metrics reporter")
                     underlying = new TopicReporter(Metrics.defaultRegistry(), new ProducerConfig(props.props), "broker%s".format(props.getString("broker.id")))
                   }
                 }
  }
}
