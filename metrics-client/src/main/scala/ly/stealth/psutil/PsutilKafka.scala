package ly.stealth.psutil

import java.util.UUID
import ly.stealth.kafka.riemann.PsutilMetricsConsumer

object PsutilKafka extends App {
  val consumer = new PsutilMetricsConsumer(riemannHost = args(1),
                                           riemannPort = args(2).toInt,
                                           descriptionText = "basic description",
                                           tagList = "kafka",
                                           groupId = UUID.randomUUID().toString,
                                           topic = args(0),
                                           zookeeperConnect = args(3))
  consumer.transfer()
}
