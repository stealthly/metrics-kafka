package ly.stealth.kafka.riemann

import ly.stealth.psutil.entities.PsutilsReport

class PsutilMetricsConsumer(riemannHost: String,
                             riemannPort: Integer,
                             descriptionText: String,
                             tagList: String,
                             topic: String,
                             groupId: String,
                             zookeeperConnect: String,
                             zkSessionTimeoutMs: Int = 30000,
                             readFromStartOfStream: Boolean = true,
                             stateMatcher: (String, Double) => String = null,
                             defaultState: String = "info") extends RiemannMetricsConsumer(riemannHost, riemannPort,
                                                                                           descriptionText, tagList,
                                                                                           topic, groupId,
                                                                                           zookeeperConnect, zkSessionTimeoutMs,
                                                                                           readFromStartOfStream,
                                                                                           stateMatcher,defaultState) {
  def transfer[T](onlyOnce: Boolean) = {
    info("reading on stream now")
    val it = stream.iterator()
    while (it.hasNext) {
      val messageAndTopic = it.next
      try {
        val report = mapper.readValue(messageAndTopic.message(), classOf[PsutilsReport])
        if (report.boot_time > 0) {
          sendMetric("boot time", report.boot_time)
        }
        if (report.cpu_count > 0) {
          sendMetric("cpu count", report.cpu_count, "cpu")
        }
        if (report.cpu_percent > 0) {
          sendMetric("cpu percent", report.cpu_percent, "cpu")
        }
        if (report.virtual_memory != null) {
          sendMetric("memory used %", report.virtual_memory.percent, "memory")
          sendMetric("memory used", report.virtual_memory.total, "memory")
          sendMetric("memory free", report.virtual_memory.free, "memory")
          sendMetric("memory used", report.virtual_memory.used, "memory")
          sendMetric("memory available", report.virtual_memory.available, "memory")
          sendMetric("memory buffers", report.virtual_memory.buffers, "memory")
          sendMetric("memory total", report.virtual_memory.total, "memory")
          sendMetric("memory inactive", report.virtual_memory.inactive, "memory")
          sendMetric("memory active", report.virtual_memory.active, "memory")
          sendMetric("memory cached", report.virtual_memory.cached, "memory")
        }
        if (report.disk_usage != null) {
          sendMetric("disk used %", report.disk_usage.percent, "disk")
          sendMetric("disk used", report.disk_usage.used, "disk")
          sendMetric("disk free", report.disk_usage.free, "disk")
          sendMetric("disk total", report.disk_usage.total, "disk")
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
}
