package com.kynsof.identity.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Configuración para servicios externos como CloudBridges
 */
@Configuration
public class ExternalServicesConfig {

    @Value("${external.services.cloudbridges.url:http://localhost:8097}")
    private String cloudBridgesBaseUrl;
    
    @Value("${external.services.cloudbridges.files.endpoint:/api/files/upload}")
    private String cloudBridgesFilesEndpoint;
    
    @Value("${external.services.cloudbridges.email.endpoint:/api/mail/send/email}")
    private String cloudBridgesEmailEndpoint;

    /**
     * URL completa para el endpoint de subida de archivos en CloudBridges
     */
    @Bean
    public String cloudBridgesFileUploadUrl() {
        return UriComponentsBuilder.fromHttpUrl(cloudBridgesBaseUrl)
                .path(cloudBridgesFilesEndpoint)
                .toUriString();
    }
    
    /**
     * URL completa para el endpoint de envío de correos en CloudBridges
     */
    @Bean
    public String cloudBridgesEmailUrl() {
        return UriComponentsBuilder.fromHttpUrl(cloudBridgesBaseUrl)
                .path(cloudBridgesEmailEndpoint)
                .toUriString();
    }
}
