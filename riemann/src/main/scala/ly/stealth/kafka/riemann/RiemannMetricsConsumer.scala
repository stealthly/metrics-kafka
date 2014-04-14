package ly.stealth.kafka.riemann

import kafka.consumer.{ConsumerConfig, Consumer, Whitelist}
import kafka.serializer._
import java.util.Properties
import kafka.utils.Logging
import scala.collection.JavaConversions._
import ly.stealth.kafka.metrics.KafkaMetricsReport
import com.google.gson.Gson
import net.benmur.riemann.client._
import RiemannClient._
import akka.util.Timeout
import akka.actor.ActorSystem
import java.net.InetSocketAddress

class RiemannMetricsConsumer(topic: String,
                             groupId: String,
                             zookeeperConnect: String,
                             readFromStartOfStream: Boolean = true) extends Logging {

  implicit val system = ActorSystem()
  implicit val timeout = Timeout(5)
  val metricsDestination = riemannConnectAs[Reliable] to new InetSocketAddress("localhost", 5555)

  val props = new Properties()
  props.put("group.id", groupId)
  props.put("zookeeper.connect", zookeeperConnect)
  props.put("auto.offset.reset", if(readFromStartOfStream) "smallest" else "largest")


  val config = new ConsumerConfig(props)
  val connector = Consumer.create(config)

  val filterSpec = new Whitelist(topic)

  info("setup:start topic=%s for zk=%s and groupId=%s".format(topic,zookeeperConnect,groupId))
  val stream = connector.createMessageStreamsByFilter(filterSpec, 1, new DefaultDecoder(), new DefaultDecoder()).get(0)
  info("setup:complete topic=%s for zk=%s and groupId=%s".format(topic,zookeeperConnect,groupId))

  val mapper = new Gson();

  def read(write: (Array[Byte])=>Unit)  = {
    info("reading on stream now")
    for(messageAndTopic <- stream) {
      try {
        val report = mapper.fromJson(new String(messageAndTopic.message()), classOf[KafkaMetricsReport])
        //TODO: push report to riemann
      } catch {
        case e: Throwable =>
          error("Error processing message, skipping this message: ", e)
          throw e
      }
    }
  }

  def close() {
    connector.shutdown()
  }
}