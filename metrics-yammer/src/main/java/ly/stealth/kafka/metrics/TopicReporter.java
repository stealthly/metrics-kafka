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

package ly.stealth.kafka.metrics;

import com.yammer.metrics.core.*;
import com.yammer.metrics.reporting.AbstractPollingReporter;
import com.yammer.metrics.stats.Snapshot;
import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;
import static java.lang.String.valueOf;

public class TopicReporter extends AbstractPollingReporter implements MetricProcessor<Context> {
    private final Map<MetricName, Producer> producerMap = new HashMap<MetricName, Producer>();
    private final MetricPredicate predicate = MetricPredicate.ALL;
    private final Clock clock = Clock.defaultClock();
    private final ProducerConfig producerConfig;
    private final String prefix;

    private Long startTime = 0L;

    public TopicReporter(MetricsRegistry metricsRegistry, ProducerConfig producerConfig, String prefix) {
        super(metricsRegistry, "kafka-topic-reporter");
        this.producerConfig = producerConfig;
        this.prefix = prefix;
    }

    public void run() {
        final Set<Map.Entry<MetricName, Metric>> metrics=getMetricsRegistry().allMetrics().entrySet();
        try {
            for (Map.Entry<MetricName, Metric> entry : metrics) {
                final MetricName metricName = entry.getKey();
                final Metric metric = entry.getValue();
                if (predicate.matches(metricName, metric)) {
                    final Context context = new Context() {
                        public Producer getProducer() {
                            return getActualProducer(metricName);
                        }
                    };
                    metric.processWith(this, entry.getKey(), context);
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void processMeter(MetricName name, Metered meter, Context context) {
        final String header = "# time,count,1 min rate,mean rate,5 min rate,15 min rate";
        final Producer producer = context.getProducer();
        final String topic = "%s-metrics-meter".format(prefix);
        final String message = valueOf(meter.count()) + ',' + meter.oneMinuteRate() + ',' + meter.meanRate() + ','
                + meter.fiveMinuteRate() + ',' + meter.fifteenMinuteRate();
        send(producer, header, topic, message);
    }

    public void processCounter(MetricName name, Counter counter, Context context) {
        final String header = "# time,count";
        final Producer producer = context.getProducer();
        final String topic = "%s-metrics-counter".format(prefix);
        final String message = valueOf(counter.count());
        send(producer, header, topic, message);
    }

    public void processHistogram(MetricName name, Histogram histogram, Context context) {
        final String header = "# time,min,max,mean,median,stddev,95%,99%,99.9%";
        final Producer producer = context.getProducer();
        final Snapshot snapshot = histogram.getSnapshot();
        final String topic="%s-metrics-histogram".format(prefix);
        final String message = valueOf(histogram.min()) + ',' + histogram.max() + ',' + histogram.mean() + ','
                + snapshot.getMedian() + ',' + histogram.stdDev() + ',' + snapshot.get95thPercentile() + ',' + snapshot.get99thPercentile() + ','
                + snapshot.get999thPercentile();
        send(producer, header, topic, message);
    }

    public void processTimer(MetricName name, Timer timer, Context context) {
        final String header = "# time,min,max,mean,median,stddev,95%,99%,99.9%";
        final Producer producer = context.getProducer();
        final Snapshot snapshot = timer.getSnapshot();
        final String topic="%s-metrics-timer".format(prefix);
        final String  message = valueOf(timer.min()) + ',' + timer.max() + ',' + timer.mean() + ',' + snapshot.getMedian() + ','
                + timer.stdDev() + ',' + snapshot.get95thPercentile() + ',' + snapshot.get99thPercentile() + ',' + snapshot.get999thPercentile();
        send(producer, header, topic, message);
    }

    public void processGauge(MetricName name, Gauge<?> gauge, Context context){
        final String header = "# time,finalue";
        final Producer producer = context.getProducer();
        final String topic = "%s-metrics-gauge".format(prefix);
        final String message = gauge.value().toString();
        send(producer, header, topic, message);
    }

    @Override
    public void start(long period, TimeUnit unit) {
        this.startTime = clock.time();
        super.start(period, unit);
    }

    @Override
    public void shutdown() {
        try {
            super.shutdown();
        } finally {
            for (Producer producer : producerMap.values()) {
                producer.close();
            }
        }
    }

    private Producer getActualProducer(MetricName metricName) {
        Producer producer;
        synchronized(producerMap) {
            producer = producerMap.get(metricName);
            if (producer == null) {
                producer = new Producer(producerConfig);
                producerMap.put(metricName, producer);
            }
        }
        return producer;
    }

    private void send(Producer producer,String header, String topic, String message) {
        final Long time = TimeUnit.MILLISECONDS.toSeconds(clock.time() - startTime);
        try {
            producer.send(new KeyedMessage(topic, format("%s\n%d,%s", header, time, message).getBytes("UTF-8")));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}

interface Context{
    public Producer getProducer();
}