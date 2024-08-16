package net.datto.dciservice.services;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class MetricsService {

    @Autowired
    private MeterRegistry meterRegistry;

    /**
     * Start a Timer for the specified metricClass and the *names*.

     * @param metricClass The class associated with the timer.
     * *
     * @param names The names to store with the timer.
     * *
     * @return The [com.codahale.metrics.Timer.Context] started with the timer starts. This
     * *         must be closed either explicilty or via the try with resource.
     */
    public Timer startTimer(Class metricClass, String... names)  {
        return meterRegistry.timer(getName(metricClass, names));
    }

    /**
     * Increment a Meter for the specified metricClass and the *names*.

     * @param metricClass The class associated with the timer.
     * *
     * @param names The names to store with the timer.
     * *
     */
    public void markMeter(Class metricClass, String... names) {
        meterRegistry.counter(getName(metricClass, names)).increment();
    }

    /**
     * build up the name pf the timer based on the supplied informatio.

     * @param metricClass The class that the metrics are associated with.
     * *
     * @param names The names of the metrics
     * *
     * @return The build up name for the metric event.
     */
    private String getName(Class metricClass, String... names) {
        List<String> namesList = new ArrayList<>(Arrays.asList(names));
        return getMetricName(metricClass, namesList.toArray(new String[0]));
    }

    private String getMetricName(Class metricClass, String[] names) {
        StringBuilder metricName = new StringBuilder(metricClass.getName());
        for (String name : names) {
            metricName.append(".").append(name);
        }
        return metricName.toString();
    }
}

