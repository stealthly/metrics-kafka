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
 package ly.stealth.kafka.metrics

import java.util.UUID
import com.codahale.metrics.MetricRegistry
import ly.stealth.kafka.riemann.CodaHaleMetricsConsumer
import java.net.InetSocketAddress
import net.benmur.riemann.client._
import RiemannClient._
import akka.actor.ActorSystem
import akka.util.Timeout
import scala.concurrent.duration.Duration
import scala.concurrent.Await
import org.specs2.mutable._
import kafka.utils.Logging

class RiemannMetricsConsumerSpec extends Specification with Logging {

  val zkConnection: String = "192.168.86.5:2181"
  val kafkaConnection: String = "192.168.86.10:9092"
  val riemannHost: String = "192.168.86.55"
  val riemannPort: Int = 5555
  val registry = new MetricRegistry()
  val metricName: String = "test_counter"
  val counter = registry.counter(metricName)
  val topic = UUID.randomUUID().toString

  "KafkaReporter" should {
    "be able to write metrics to Kafka topic" in {
      val r = new scala.util.Random
      counter.inc(10)
      
      var success = true
      try {
        val producer = KafkaReporter.builder(registry, kafkaConnection, topic).build()
        producer.report()
      } catch {
        case e: Exception => success = false
      }

      success must beTrue
    }
  }
  
  "RiemannMetricsConsumer" should {
    "be able to transfer metrics from Kafka topic to Riemann" in {      
      val groupId = UUID.randomUUID().toString
      var success = true
      try {
        val consumer = new CodaHaleMetricsConsumer(riemannHost, riemannPort, "basic description", "kafka", topic, groupId, zkConnection, 30000)
        consumer.transfer(true)
      } catch {
        case e: Exception => success = false
      }

      success must beTrue
    }
  }

  "Riemann users" should {
    "be able to query metrics, written with RiemannMetricsConsumer, from Riemann" in {
      implicit val system = ActorSystem()
      implicit val timeout = Timeout(30)
      val metricsDestination = riemannConnectAs[Reliable] to new InetSocketAddress(riemannHost, riemannPort)
      val future = metricsDestination ask Query("tagged \"kafka\"")
      Await.ready(future, Duration.Inf)
      val events = future.value.get.get

      events must not(beEmpty)
      events.last.service.get mustEqual "%s [total]".format(metricName)
      events.last.metric.get mustEqual counter.getCount.toDouble
    }
  }
}