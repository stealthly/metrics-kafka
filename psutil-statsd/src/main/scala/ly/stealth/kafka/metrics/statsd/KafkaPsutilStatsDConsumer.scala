/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ly.stealth.kafka.metrics.statsd

import java.util.Properties
import java.util.concurrent.atomic.AtomicBoolean

import akka.actor.ActorContext
import com.fasterxml.jackson.databind.ObjectMapper
import kafka.consumer.{Consumer, ConsumerConfig, Whitelist}
import kafka.serializer.StringDecoder
import kafka.utils.Logging
import ly.stealth.metrics.kafka.statsd.StatsD
import ly.stealth.psutil.entities.PsutilsReport

class KafkaPsutilStatsDConsumer(actorContext: ActorContext = null,
                                statsdHost: String,
                                statsdPort: Integer,
                                multiMetrics: Boolean = true,
                                packetBufferSize: Int = 1024,
                                topic: String,
                                groupId: String,
                                zookeeperConnect: String,
                                zkSessionTimeoutMs: Int = 30000,
                                readFromStartOfStream: Boolean = true) extends Logging {
  val props = new Properties()
  props.put("group.id", groupId)
  props.put("zookeeper.connect", zookeeperConnect)
  props.put("auto.offset.reset", if (readFromStartOfStream) "smallest" else "largest")
  props.put("zookeeper.session.timeout.ms", zkSessionTimeoutMs.toString)

  val config = new ConsumerConfig(props)
  val connector = Consumer.create(config)

  val filterSpec = new Whitelist(topic)
  val mapper = new ObjectMapper

  info("Trying to start consumer: topic=%s for zk=%s and groupId=%s".format(topic, zookeeperConnect, groupId))
  val stream = connector.createMessageStreamsByFilter(filterSpec, 1, new StringDecoder(), new StringDecoder()).head
  info("Started consumer: topic=%s for zk=%s and groupId=%s".format(topic, zookeeperConnect, groupId))

  val client = new StatsD(actorContext, statsdHost, statsdPort, multiMetrics, packetBufferSize)
  val stopped = new AtomicBoolean(false)
  
  def start() = {
    new Thread(new Runnable {
      def run(): Unit = {
        info("reading on stream now")
        val it = stream.iterator()
        while (it.hasNext) {
          val messageAndTopic = it.next
          try {
            val report = mapper.readValue(messageAndTopic.message(), classOf[PsutilsReport])
            if (report.boot_time > 0) {
              client.timing("boot time", report.boot_time)
            }
            if (report.cpu_count > 0) {
              client.gauge("cpu count", report.cpu_count)
            }
            if (report.cpu_percent > 0) {
              client.gauge("cpu percent", report.cpu_percent)
            }
            if (report.virtual_memory != null) {
              client.gauge("memory used %", report.virtual_memory.percent)
              client.gauge("memory used", report.virtual_memory.total)
              client.gauge("memory free", report.virtual_memory.free)
              client.gauge("memory used", report.virtual_memory.used)
              client.gauge("memory available", report.virtual_memory.available)
              client.gauge("memory buffers", report.virtual_memory.buffers)
              client.gauge("memory total", report.virtual_memory.total)
              client.gauge("memory inactive", report.virtual_memory.inactive)
              client.gauge("memory active", report.virtual_memory.active)
              client.gauge("memory cached", report.virtual_memory.cached)
            }
            if (report.disk_usage != null) {
              client.gauge("disk used %", report.disk_usage.percent)
              client.gauge("disk used", report.disk_usage.used)
              client.gauge("disk free", report.disk_usage.free)
              client.gauge("disk total", report.disk_usage.total)
            }
          } catch {
            case e: Throwable =>
              if (stopped.get()) {
                info("consumer worker has been stoppped")
                return
              } else {
                warn("Error processing message, skipping this message: ", e)
              }
          }
        }
      }
    }).start()
  }
  
  def stop() = {
    info("stopping consumer")
    connector.shutdown()
    stopped.set(true)
    info("stopped consumer")
  }
}
