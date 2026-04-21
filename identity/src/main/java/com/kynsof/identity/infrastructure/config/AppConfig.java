package com.kynsof.identity.infrastructure.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.util.Timeout;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class AppConfig {

    @Value("${rest.http.pool.max-total:200}")
    private int maxTotal;

    @Value("${rest.http.pool.default-max-per-route:50}")
    private int defaultMaxPerRoute;

    @Value("${rest.http.timeout.connection:10000}")
    private int connectionTimeout;

    @Value("${rest.http.timeout.request:10000}")
    private int requestTimeout;

    @Value("${rest.http.timeout.socket:30000}")
    private int socketTimeout;

    @Bean
    public RestTemplate restTemplate() {
        // === 1. Connection Pool Configuration ===
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(maxTotal); // Total de conexiones en el pool
        connectionManager.setDefaultMaxPerRoute(defaultMaxPerRoute); // Conexiones por ruta (dominio)

        // Configuración de timeouts y keep-alive
        ConnectionConfig connectionConfig = ConnectionConfig.custom()
                .setConnectTimeout(Timeout.ofMilliseconds(connectionTimeout))
                .setSocketTimeout(Timeout.ofMilliseconds(socketTimeout))
                .build();
        connectionManager.setDefaultConnectionConfig(connectionConfig);

        // === 2. Request Configuration ===
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(Timeout.ofMilliseconds(requestTimeout))
                .setResponseTimeout(Timeout.ofMilliseconds(socketTimeout))
                .build();

        // === 3. HTTP Client con pooling ===
        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
                .evictExpiredConnections() // Elimina conexiones expiradas automáticamente
                .evictIdleConnections(Timeout.ofSeconds(60)) // Elimina conexiones inactivas después de 60s
                .build();

        // === 4. Request Factory con el HTTP Client optimizado ===
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);

        // === 5. JSON Configuration (mantener configuración original) ===
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
        jsonConverter.setObjectMapper(objectMapper);

        List<MediaType> mediaTypes = new ArrayList<>(jsonConverter.getSupportedMediaTypes());
        mediaTypes.add(MediaType.APPLICATION_JSON);
        mediaTypes.add(MediaType.APPLICATION_JSON_UTF8);
        mediaTypes.add(new MediaType("application", "*+json", StandardCharsets.UTF_8));
        jsonConverter.setSupportedMediaTypes(mediaTypes);

        // === 6. RestTemplate con pooling configurado ===
        RestTemplate restTemplate = new RestTemplate(requestFactory);

        // Agrega convertidores (mantener configuración original)
        restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));
        restTemplate.getMessageConverters().add(1, jsonConverter);

        return restTemplate;
    }
}
