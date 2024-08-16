package net.datto.dciservice.configuration;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.config.validate.Validated;
import io.micrometer.core.instrument.config.validate.ValidationException;
import io.micrometer.graphite.GraphiteConfig;
import io.micrometer.graphite.GraphiteMeterRegistry;
import io.micrometer.graphite.GraphiteProtocol;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class SpringTestConfig {

    @Bean
    public GraphiteMeterRegistry mockGraphiteMeterRegistry(GraphiteConfig graphiteConfig, Clock clock) {
        return new GraphiteMeterRegistry(graphiteConfig, clock);
    }

    @Bean
    public BuildProperties mockBuildProperties() {
        return mock(BuildProperties.class);
    }

    @Bean
    public GraphiteConfig mockGraphiteConfig() {
        return new MockGraphiteConfig();
    }

    private static class MockGraphiteConfig implements GraphiteConfig {
        @Override
        public Duration step() {
            return Duration.ofMillis(1000);
        }

        @Override
        public Validated<?> validate() {
            return null;
        }

        @Override
        public void requireValid() throws ValidationException {
        }

        @Override
        public String get(String s) {
            return null;
        }

        @Override
        public String prefix() {
            return "prefix";
        }

        @Override
        public String[] tagsAsPrefix() {
            return new String[0];
        }

        @Override
        public TimeUnit rateUnits() {
            return TimeUnit.MINUTES;
        }

        @Override
        public TimeUnit durationUnits() {
            return TimeUnit.MINUTES;
        }

        @Override
        public String host() {
            return "test";
        }

        @Override
        public int port() {
            return 0;
        }

        @Override
        public boolean enabled() {
            return false;
        }

        @Override
        public GraphiteProtocol protocol() {
            return GraphiteProtocol.PLAINTEXT;
        }
    }
}
