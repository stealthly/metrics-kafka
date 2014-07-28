package ly.stealth.psutil

import java.util.UUID
import java.util.concurrent.TimeUnit
import ly.stealth.kafka.metrics.KafkaGraphiteConsumer
import ly.stealth.kafka.metrics.statsd.KafkaPsutilStatsDConsumer
import ly.stealth.kafka.riemann.PsutilMetricsConsumer

object PsutilKafka extends App {
  val riemann = new Thread(new Runnable() {
    def run(): Unit = {
      val consumer = new PsutilMetricsConsumer(riemannHost = args(1),
        riemannPort = 5555,
        descriptionText = "basic description",
        tagList = "kafka",
        groupId = UUID.randomUUID().toString,
        topic = args(0),
        zookeeperConnect = args(3))
      consumer.transfer()
    }
  })

  new KafkaPsutilStatsDConsumer(null, args(1), 8125, false, 1024, args(0),UUID.randomUUID().toString, args(3), 30000, true).start()
  new KafkaGraphiteConsumer(args(1), 2003, args(3), "kafka-metrics-topic", UUID.randomUUID().toString, 30000, true).start()
  riemann.start()

  TimeUnit.SECONDS.sleep(30)

  System.exit(0)
}
