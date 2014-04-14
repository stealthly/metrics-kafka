package ly.stealth.kafka.metrics;

import com.codahale.metrics.MetricRegistry;
import com.google.gson.Gson;
import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.assertNotNull;

public class MetricsReportingSteps {
    private final String zkConnect = "192.168.86.5:2181";
    private final String kafkaConnect = "192.168.86.10:9092";
    private final String topic = "sandbox";
    private KafkaReporter kafkaReporter;

    @Given("Kafka broker is up and 'metrics' topic is created.")
    public void startingKafkaReporterAndCon() {
        kafkaReporter = KafkaReporter.builder(new MetricRegistry(),
                                              kafkaConnect,
                                              topic).build();
    }

    @When("KafkaReporter sends data to Kafka topic.")
    public void reporterWritesMetrics() {
        kafkaReporter.report();
    }

    @Then("Kafka consumer should be able to read this data.")
    public void consumerReadsMetrics() {
        ConsumerConnector consumer = Consumer.createJavaConsumerConnector(createConsumerConfig());
        String message = readMessage(consumer);
        assertNotNull(message);
        KafkaMetricsReport report = new Gson().fromJson(message, KafkaMetricsReport.class);
        assertNotNull(report);
    }

    private ConsumerConfig createConsumerConfig() {
        Properties props = new Properties();
        props.put("zookeeper.connect", zkConnect);
        props.put("group.id", "groupId");
        props.put("auto.offset.reset", "smallest");
        return new ConsumerConfig(props);
    }

    public String readMessage(ConsumerConnector consumer) {
        Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
        topicCountMap.put(topic, new Integer(1));
        Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = consumer.createMessageStreams(topicCountMap);
        KafkaStream<byte[], byte[]> stream =  consumerMap.get(topic).get(0);
        ConsumerIterator<byte[], byte[]> it = stream.iterator();

        return new String(it.next().message());
    }
}
