package ly.stealth.kafka.metrics;

import java.io.Serializable;
import java.util.SortedMap;

/**
 * Created by ilyutov on 4/10/14.
 */
public class KafkaMetricsReport implements Serializable {
    private static final long serialVersionUID = 1L;

    private String version;
    private SortedMap<String, Gauge> gauges;
    private SortedMap<String, Counter> counters;
    private SortedMap<String, Histogram> histograms;
    private SortedMap<String, Meter> meters;
    private SortedMap<String, Timer> timers;

    public String getVersion() {
        return version;
    }

    public SortedMap<String, Gauge> getGauges() {
        return gauges;
    }

    public SortedMap<String, Counter> getCounters() {
        return counters;
    }

    public SortedMap<String, Histogram> getHistograms() {
        return histograms;
    }

    public SortedMap<String, Meter> getMeters() {
        return meters;
    }

    public SortedMap<String, Timer> getTimers() {
        return timers;
    }

    public static class Gauge {
        private Object value;

        public Object getValue() {
            return value;
        }
    }

    public static class Counter {
        private int count;

        public int getCount() {
            return count;
        }
    }

    public static class Histogram {
        private long count;
        private long max;
        private long mean;
        private long min;
        private long p50;
        private long p75;
        private long p95;
        private long p98;
        private long p99;
        private long p999;
        private long stddev;
        private Object values;

        public long getCount() {
            return count;
        }

        public long getMax() {
            return max;
        }

        public long getMean() {
            return mean;
        }

        public long getMin() {
            return min;
        }

        public long getP50() {
            return p50;
        }

        public long getP75() {
            return p75;
        }

        public long getP95() {
            return p95;
        }

        public long getP98() {
            return p98;
        }

        public long getP99() {
            return p99;
        }

        public long getP999() {
            return p999;
        }

        public long getStddev() {
            return stddev;
        }

        public Object getValues() {
            return values;
        }
    }

    public static class Meter {
        private long count;
        private long m15_rate;
        private long m1_rate;
        private long m5_rate;
        private long mean_rate;
        private String units;

        public long getCount() {
            return count;
        }

        public long getM15_rate() {
            return m15_rate;
        }

        public long getM1_rate() {
            return m1_rate;
        }

        public long getM5_rate() {
            return m5_rate;
        }

        public long getMean_rate() {
            return mean_rate;
        }

        public String getUnits() {
            return units;
        }
    }

    public static class Timer {
        private long count;
        private long max;
        private long mean;
        private long min;

        private long p50;
        private long p75;
        private long p95;
        private long p98;
        private long p99;
        private long p999;

        private long stddev;
        private long m15_rate;
        private long m1_rate;
        private long m5_rate;
        private long mean_rate;

        private String duration_units;
        private String rate_units;

        private Object values;

        public long getCount() {
            return count;
        }

        public long getMax() {
            return max;
        }

        public long getMean() {
            return mean;
        }

        public long getMin() {
            return min;
        }

        public long getP50() {
            return p50;
        }

        public long getP75() {
            return p75;
        }

        public long getP95() {
            return p95;
        }

        public long getP98() {
            return p98;
        }

        public long getP99() {
            return p99;
        }

        public long getP999() {
            return p999;
        }

        public long getStddev() {
            return stddev;
        }

        public long getM15_rate() {
            return m15_rate;
        }

        public long getM1_rate() {
            return m1_rate;
        }

        public long getM5_rate() {
            return m5_rate;
        }

        public long getMean_rate() {
            return mean_rate;
        }

        public String getDuration_units() {
            return duration_units;
        }

        public String getRate_units() {
            return rate_units;
        }

        public Object getValues() {
            return values;
        }
    }
}
