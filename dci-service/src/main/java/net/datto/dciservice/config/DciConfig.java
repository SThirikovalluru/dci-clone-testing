package net.datto.dciservice.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.http.io.SocketConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

@Data
@Slf4j
@Configuration
@ConfigurationProperties(prefix = "dci")
public class DciConfig {

    private Integer connectTimeout;
    private Integer readTimeout;
    private String portalApiUrl = "https://api.datto.com";
    private String platform;

    @Bean
    @Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
    public RestTemplate restTemplate() {
        var socketConfig = SocketConfig.custom()
                .setSoTimeout(this.readTimeout, MILLISECONDS)
                .build();

        var connectionManager = PoolingHttpClientConnectionManagerBuilder
                .create()
                .setDefaultSocketConfig(socketConfig)
                .build();

        var httpClient = HttpClientBuilder
                .create()
                .setConnectionManager(connectionManager)
                .build();

        var factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        factory.setConnectTimeout(connectTimeout);

        log.debug("RestTemplate initialized with portalApiUrl: {}, connectTimeout: {}, readTimeout:{}", portalApiUrl, connectTimeout, readTimeout);
        return new RestTemplate(factory);
    }
}
