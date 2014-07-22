package ly.stealth.kafka.metrics;

import com.fasterxml.jackson.databind.ObjectMapper;
import kafka.consumer.*;
import kafka.message.MessageAndMetadata;
import kafka.serializer.StringDecoder;
import ly.stealth.graphite.GraphiteClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

public class KafkaGraphiteConsumer {
    private static final Logger log = LoggerFactory.getLogger(KafkaGraphiteConsumer.class);
    private ConsumerConnector consumerConnector;
    private KafkaStream<String, String> messageStream;
    private GraphiteClient graphiteClient;
    private ObjectMapper mapper = new ObjectMapper();
    private AtomicBoolean stopped = new AtomicBoolean(false);

    public KafkaGraphiteConsumer(String graphiteHost,
                                 int graphitePort,
                                 String zkConnect,
                                 String topic,
                                 String groupId,
                                 int zkSessionTimeoutMs,
                                 boolean readFromStartOfStream) {
        Properties props = new Properties();
        props.put("group.id", groupId);
        props.put("zookeeper.connect", zkConnect);
        props.put("auto.offset.reset", readFromStartOfStream ? "smallest" : "largest");
        props.put("zookeeper.session.timeout.ms", String.valueOf(zkSessionTimeoutMs));

        ConsumerConfig config = new ConsumerConfig(props);
        consumerConnector = Consumer.create(config);
        TopicFilter filterSpec = new Whitelist(topic);

        log.info("Trying to start consumer: topic=%s for zk=%s and groupId=%s".format(topic, zkConnect, groupId));
        messageStream = consumerConnector.createMessageStreamsByFilter(filterSpec,
                1,
                new StringDecoder(null),
                new StringDecoder(null)).head();
        log.info("Started consumer: topic=%s for zk=%s and groupId=%s".format(topic, zkConnect, groupId));
        graphiteClient = new GraphiteClient(graphiteHost, graphitePort);
    }

    public void start() {
        new Thread(new Runnable() {
            public void run() {
                log.info("reading on stream now");
                ConsumerIterator<String, String> it = messageStream.iterator();
                while (it.hasNext()) {
                    MessageAndMetadata<String, String> messageAndTopic = it.next();
                    try {
                        KafkaMetricsReport report = mapper.readValue(messageAndTopic.message(), KafkaMetricsReport.class);
                        if (report.getCounters() != null) {
                            for (Map.Entry<String, KafkaMetricsReport.Counter> entry : report.getCounters().entrySet()) {
                                graphiteClient.sendMetric("%s [%s]".format(entry.getKey(), "total"), entry.getValue().getCount());
                            }
                        }

                        if (report.getHistograms() != null) {
                            for (Map.Entry<String, KafkaMetricsReport.Histogram> entry : report.getHistograms().entrySet()) {
                                graphiteClient.sendMetric("%s [%s]".format(entry.getKey(), "total"), entry.getValue().getCount());
                                graphiteClient.sendMetric("%s [%s]".format(entry.getKey(), "max"), entry.getValue().getMax());
                                graphiteClient.sendMetric("%s [%s]".format(entry.getKey(), "min"), entry.getValue().getMin());
                                graphiteClient.sendMetric("%s [%s]".format(entry.getKey(), "mean"), entry.getValue().getMean());
                                graphiteClient.sendMetric("%s [%s]".format(entry.getKey(), "P50"), entry.getValue().getP50());
                                graphiteClient.sendMetric("%s [%s]".format(entry.getKey(), "P75"), entry.getValue().getP75());
                                graphiteClient.sendMetric("%s [%s]".format(entry.getKey(), "P95"), entry.getValue().getP95());
                                graphiteClient.sendMetric("%s [%s]".format(entry.getKey(), "P98"), entry.getValue().getP98());
                                graphiteClient.sendMetric("%s [%s]".format(entry.getKey(), "P99"), entry.getValue().getP99());
                                graphiteClient.sendMetric("%s [%s]".format(entry.getKey(), "P999"), entry.getValue().getP999());
                                graphiteClient.sendMetric("%s [%s]".format(entry.getKey(), "standard deviation"), entry.getValue().getStddev());
                            }
                        }

                        if (report.getTimers() != null) {
                            for (Map.Entry<String, KafkaMetricsReport.Timer> entry : report.getTimers().entrySet()) {
                                graphiteClient.sendMetric("%s [%s(%s)]".format(entry.getKey(), "max duration", entry.getValue().getDuration_units()), entry.getValue().getMax());
                                graphiteClient.sendMetric("%s [%s(%s)]".format(entry.getKey(), "min duration", entry.getValue().getDuration_units()), entry.getValue().getMin());
                                graphiteClient.sendMetric("%s [%s(%s)]".format(entry.getKey(), "mean duration", entry.getValue().getDuration_units()), entry.getValue().getMean());
                                graphiteClient.sendMetric("%s [%s(%s)]".format(entry.getKey(), "P50 duration", entry.getValue().getDuration_units()), entry.getValue().getP50());
                                graphiteClient.sendMetric("%s [%s(%s)]".format(entry.getKey(), "P75 duration", entry.getValue().getDuration_units()), entry.getValue().getP75());
                                graphiteClient.sendMetric("%s [%s(%s)]".format(entry.getKey(), "P95 duration", entry.getValue().getDuration_units()), entry.getValue().getP95());
                                graphiteClient.sendMetric("%s [%s(%s)]".format(entry.getKey(), "P98 duration", entry.getValue().getDuration_units()), entry.getValue().getP98());
                                graphiteClient.sendMetric("%s [%s(%s)]".format(entry.getKey(), "P99 duration", entry.getValue().getDuration_units()), entry.getValue().getP99());
                                graphiteClient.sendMetric("%s [%s(%s)]".format(entry.getKey(), "P999 duration", entry.getValue().getDuration_units()), entry.getValue().getP999());

                                graphiteClient.sendMetric("%s [%s(%s)]".format(entry.getKey(), "M1 rate", entry.getValue().getRate_units()), entry.getValue().getM1_rate());
                                graphiteClient.sendMetric("%s [%s(%s)]".format(entry.getKey(), "M5 rate", entry.getValue().getRate_units()), entry.getValue().getM5_rate());
                                graphiteClient.sendMetric("%s [%s(%s)]".format(entry.getKey(), "M15 rate", entry.getValue().getRate_units()), entry.getValue().getM15_rate());
                                graphiteClient.sendMetric("%s [%s(%s)]".format(entry.getKey(), "mean rate", entry.getValue().getRate_units()), entry.getValue().getMean_rate());

                                graphiteClient.sendMetric("%s [%s(%s)]".format(entry.getKey(), "standard deviation", entry.getValue().getRate_units()), entry.getValue().getStddev());
                            }
                        }

                        if (report.getMeters() != null) {
                            for (Map.Entry<String, KafkaMetricsReport.Meter> entry : report.getMeters().entrySet()) {
                                graphiteClient.sendMetric("%s [%s(%s)]".format(entry.getKey(), "total", entry.getValue().getUnits()), entry.getValue().getCount());
                                graphiteClient.sendMetric("%s [%s(%s)]".format(entry.getKey(), "M1 rate", entry.getValue().getUnits()), entry.getValue().getM1_rate());
                                graphiteClient.sendMetric("%s [%s(%s)]".format(entry.getKey(), "M5 rate", entry.getValue().getUnits()), entry.getValue().getM5_rate());
                                graphiteClient.sendMetric("%s [%s(%s)]".format(entry.getKey(), "M15 rate", entry.getValue().getUnits()), entry.getValue().getM15_rate());
                                graphiteClient.sendMetric("%s [%s(%s)]".format(entry.getKey(), "mean rate", entry.getValue().getUnits()), entry.getValue().getMean_rate());
                            }
                        }
                    } catch (Throwable e) {
                        if (stopped.get()) {
                            log.info("Consumer worker has been stopped");
                            return;
                        } else {
                            log.warn("Error processing message, skipping this message: ", e);
                        }
                    }
                }
            }
        }).start();
    }

    public void stop() {
        log.info("Trying to stop consumer");
        consumerConnector.shutdown();
        stopped.set(true);
        log.info("Consumer has been stopped");
    }
}
