package net.datto.dciservice.config.aws;

import org.springframework.boot.logging.DeferredLog;
import org.springframework.core.env.PropertySource;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParametersByPathRequest;
import software.amazon.awssdk.services.ssm.model.GetParametersByPathResponse;
import software.amazon.awssdk.services.ssm.model.Parameter;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static net.logstash.logback.encoder.org.apache.commons.lang.StringUtils.EMPTY;

/*
 * The Parameter Store is part of AWS Systems Manager and allows us to centralise application configuration in a secure
 * and hierarchical fashion.
 *
 * Properties are stored in paths /PLATFORM_NAME/shared/property.name or /PLATFORM_NAME/APPLICATION_NAME/property.name
 * with the application specific property taking precedence over the shared property.
 *
 * This class exposes any properties from the parameter store that are applicable to the configuration of this app.
 */
public class AwsParameterStorePropertySource extends PropertySource<SsmClient> {
    private final Map<String, String> generalProperties;
    private final Map<String, String> applicationProperties;
    /**
     * This class is loaded during the spring boot start up before the logging
     * framework has been fully initialised, so defer logging until it can be logged.
     */
    private final DeferredLog log;

    public AwsParameterStorePropertySource(String name, SsmClient source,
                                           String application, String platform) {
        super(name, source);
        if (application == null) {
            throw new RuntimeException("spring.application.name not defined when loading AWS Parameter Store properties");
        }
        if (platform == null) {
            throw new RuntimeException("platform.name not defined when loading AWS Parameter Store properties");
        }

        this.log = new DeferredLog();
        this.generalProperties = new HashMap<>();
        this.applicationProperties = new HashMap<>();

        log.info("Loading AWS Parameter Store properties for " + application + " from platform " + platform);

        populateProperties(application, platform);
    }

    /***
     * Rather than querying the parameter store for a property value every time it is required, we load matching props
     * into a shared generalProperties and app specific applicationProperties hashmap when the property source is first
     * initialised
     *
     * @param applicationName The app name to match in the path /.../applicationName/...
     * @param platform The platform to match in the path /platform/.../...
     */
    private void populateProperties(String applicationName, String platform) {
        var sharedPath = String.format("/%s/shared/", platform);
        var applicationPath = String.format("/%s/%s/", platform, applicationName);

        generalProperties.putAll(populateProperties(sharedPath));
        applicationProperties.putAll(populateProperties(applicationPath));
    }


    private Map<String, String> populateProperties(String path) {
        var parametersByPathRequest = GetParametersByPathRequest.builder()
                .path(path)
                .withDecryption(true)
                .build();

        var parameters = source.getParametersByPathPaginator(parametersByPathRequest)
                .stream()
                .map(GetParametersByPathResponse::parameters)
                .flatMap(Collection::stream)
                .collect(Collectors.toMap(parameter -> parameter.name().replaceFirst(path, EMPTY), Parameter::value));

        log.info(String.format("Retrieved %s AWS system parameters for the path '%s'", parameters.size(), path));

        return parameters;
    }

    @Override
    public Object getProperty(String propertyName) {
        if (applicationProperties.containsKey(propertyName)) {
            return applicationProperties.get(propertyName);
        } else {
            return generalProperties.get(propertyName);
        }
    }

    /**
     * Write the deferred logs to the logging framework.
     */
    void writeLogs() {
        log.replayTo(AwsParameterStorePropertySource.class);
    }
}