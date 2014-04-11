package ly.stealth.kafka.metrics;

import java.util.List;

import de.codecentric.jbehave.junit.monitoring.JUnitReportingRunner;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.io.CodeLocations;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.io.StoryFinder;
import org.jbehave.core.junit.JUnitStories;
import org.jbehave.core.reporters.Format;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.InstanceStepsFactory;
import org.junit.runner.RunWith;

/**
 * Created by ilyutov on 4/11/14.
 */
@RunWith(JUnitReportingRunner.class)
public class Stories extends JUnitStories {
    @Override
    protected List<String> storyPaths() {
        return new StoryFinder().findPaths(CodeLocations.codeLocationFromPath("src/test/resources"),
                "**/*.story",
                "**/exclude_*.story");
    }

    @Override
    public Configuration configuration() {
        return new MostUsefulConfiguration()
                .useStoryLoader(new LoadFromClasspath(this.getClass()))
                .useStoryReporterBuilder(new StoryReporterBuilder()
                        .withFormats(Format.TXT)
                        .withRelativeDirectory("../build/jbehave/"));
    }

    @Override
    public InjectableStepsFactory stepsFactory() {
        return new InstanceStepsFactory(configuration(), new MetricsReportingSteps());
    }
}