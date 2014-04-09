import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.reporting.AbstractPollingReporter;

public class KafkaReporter extends AbstractPollingReporter {

    /**
     * Creates a new {@link com.yammer.metrics.reporting.AbstractPollingReporter} instance.
     *
     * @param registry the {@link com.yammer.metrics.core.MetricsRegistry} containing the metrics this reporter will
     *                 report
     * @param name     the reporter's name
     * @see AbstractReporter#AbstractReporter(com.yammer.metrics.core.MetricsRegistry)
     */
    protected KafkaReporter(MetricsRegistry registry, String name) {
        super(registry, name);
    }

    @Override
    public void run() {

    }
}
