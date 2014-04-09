import com.codahale.metrics.*;
import kafka.javaapi.producer.Producer;
import kafka.producer.ProducerConfig;

import java.util.Properties;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

public class KafkaReporter extends ScheduledReporter {
    private Producer<String, String> kafkaProducer;

    protected KafkaReporter(MetricRegistry registry,
                            String name,
                            MetricFilter filter,
                            TimeUnit rateUnit,
                            TimeUnit durationUnit,
                            Properties properties) {
        super(registry, name, filter, rateUnit, durationUnit);
        kafkaProducer = new Producer<String, String>(new ProducerConfig(properties));
    }

    @Override
    public void report(SortedMap<String, Gauge> gauges,
                       SortedMap<String, Counter> counters,
                       SortedMap<String, Histogram> histograms,
                       SortedMap<String, Meter> meters,
                       SortedMap<String, Timer> timers) {

    }
}
