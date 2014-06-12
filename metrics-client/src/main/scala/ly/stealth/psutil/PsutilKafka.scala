package ly.stealth.psutil

import java.util.UUID
import ly.stealth.kafka.riemann.PsutilMetricsConsumer

object PsutilKafka extends App {
  val command = "jar xf psutil.jar psutil_producer.py && /usr/bin/python --topic %s --async %s --sendInBatch %s url %s rm psutil_producer.py"
  val process: Process = Runtime.getRuntime.exec(command.format(args(0), args(1), args(2), args(3)))
  val consumer = new PsutilMetricsConsumer(args(4),
                                           args(5).toInt,
                                           "basic description",
                                           "kafka",
                                           args(0),
                                           UUID.randomUUID().toString,
                                           args(6),
                                           30000)
  consumer.transfer()
}
