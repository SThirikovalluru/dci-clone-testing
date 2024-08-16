package net.datto.dciservice.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.config.NamingConvention;
import io.micrometer.core.instrument.util.HierarchicalNameMapper;
import io.micrometer.graphite.GraphiteConfig;
import io.micrometer.graphite.GraphiteMeterRegistry;
import lombok.extern.slf4j.Slf4j;
import net.datto.dciservice.config.aws.AwsEcsMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.net.InetAddress;

/**
 * The configuration for the Graphite Monitoring, this will bootstrap the
 * Graphite Reporting of the Bootstrap timed events.
 */

@Slf4j
@Configuration
@ConfigurationProperties(prefix = "platform")
public class MetricsConfig {
    private String name;
    private String instance;

    @Autowired
    private AwsEcsMeta awsEcsMeta;

    @Autowired
    private ObjectMapper objectMapper;

    public MetricsConfig() {
        try {
            instance = InetAddress.getLocalHost().getHostName();
            // sometimes the ip address comes back from the above, if so replace . with -
            instance = instance.replaceAll("\\.", "_");
        } catch (Exception ex) {
            instance = "unknown";
        }
    }

    @PostConstruct
    void init() {
        try {
            // retrieve the task arn if available, example TaskARN
            // "TaskARN": "arn:aws:ecs:eu-west-1:155755667117:task/cade91ac-522e-4e4d-a036-3c5bfa434c1c"
            String ecsMeta = awsEcsMeta.getEcsTaskMeta();
            if (ecsMeta != null) {
                try {
                    JsonNode jsonNode = objectMapper.readTree(ecsMeta);
                    String taskArnProperty = jsonNode.get("TaskARN").asText();
                    String[] fields = taskArnProperty.split(":");
                    for (String field: fields) {
                        if (field.startsWith("task/")) {
                            instance = field.substring("task/".length()).replace("\\.","_");
                        }
                    }
                    log.info("Using instance name{} for Graphite metrics", instance);
                } catch (IOException e) {
                    log.warn("Unable to retrieve the TaskARN due to exception {}, so using the default instance name for Graphite",e.getMessage(), e);
                }
            } else {
                log.info("The service does not appear to be running in an ECS container, so using default instance name for Graphite");
            }
        } catch (Exception e) {
            log.warn("Exception retrieving the ECS Meta data", e);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Bean
    public GraphiteMeterRegistry graphiteMeterRegistry(GraphiteConfig config, Clock clock) {
        return new GraphiteMeterRegistry(config, clock,
                (id, convention) -> "platform." + getName() + ".dci-service." + HierarchicalNameMapper.DEFAULT.toHierarchicalName(id, NamingConvention.dot) + "." + instance);
    }

}