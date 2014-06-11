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
 package ly.stealth.kafka.riemann

import kafka.consumer.{ConsumerConfig, Consumer}
import kafka.serializer._
import java.util.Properties
import kafka.utils.Logging
import scala.collection.JavaConversions._
import net.benmur.riemann.client._
import RiemannClient._
import akka.util.Timeout
import akka.actor.ActorSystem
import java.net.InetSocketAddress
import com.fasterxml.jackson.databind.ObjectMapper
import kafka.consumer.Whitelist

abstract class RiemannMetricsConsumer(riemannHost: String,
                                      riemannPort: Integer,
                                      descriptionText: String,
                                      tagList: String,
                                      topic: String,
                                      groupId: String,
                                      zookeeperConnect: String,
                                      zkSessionTimeoutMs: Int = 30000,
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
  implicit val timeout = Timeout(15)
  val metricsDestination = riemannConnectAs[Reliable] to new InetSocketAddress(riemannHost, riemannPort) withValues (host("host")
    | description(descriptionText) | tags(tagList))

  def transfer[T](onlyOnce: Boolean = false)

  protected def sendMetric(name: String, metricValue: Double, tagged: String = null) = {
    if (tagged == null)
      state(detectMetricState(name, metricValue)) | service(name) | metric(metricValue) |>> metricsDestination
    else
      state(detectMetricState(name, metricValue)) | service(name) | metric(metricValue) | tags(tagged) |>> metricsDestination
  }

  protected def detectMetricState(name: String, metricValue: Double): String = {
    if (stateMatcher == null) defaultState
    else stateMatcher(name, metricValue)
  }

  def close() {
    connector.shutdown()
  }
}