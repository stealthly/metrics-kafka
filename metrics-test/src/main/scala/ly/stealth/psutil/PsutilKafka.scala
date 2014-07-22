package ly.stealth.psutil

import java.util.UUID
import java.util.concurrent.TimeUnit
import ly.stealth.kafka.metrics.statsd.KafkaPsutilStatsDConsumer

object PsutilKafka extends App {
  val psutilStatsdConsumer = new KafkaPsutilStatsDConsumer(null, args(1), 8125, false, 1024, args(0),
    UUID.randomUUID().toString, args(3), 30000, true)

  psutilStatsdConsumer.start()
  TimeUnit.SECONDS.sleep(30)
  psutilStatsdConsumer.stop()
}
