//package com.kynsof.share.core.infrastructure.config;
//
//import com.kynsof.share.core.infrastructure.bus.CachedMediatorImpl;
//import com.kynsof.share.core.infrastructure.bus.MediatorCacheCleanupService;
//import com.kynsof.share.core.infrastructure.bus.MediatorCacheController;
//import org.springframework.boot.autoconfigure.AutoConfiguration;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
//import org.springframework.boot.context.properties.EnableConfigurationProperties;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Import;
//import org.springframework.scheduling.annotation.EnableScheduling;
//
///**
// * Auto-configuración para el cache del Mediator
// * Se activa automáticamente en todos los microservicios cuando se habilita el cache
// */
//@AutoConfiguration
//@ConditionalOnProperty(name = "mediator.cache.enabled", havingValue = "true", matchIfMissing = false)
//@EnableConfigurationProperties(MediatorCacheProperties.class)
//@EnableScheduling
//@Import({
//    CachedMediatorImpl.class,
//    MediatorCacheCleanupService.class,
//    MediatorCacheController.class
//})
//public class MediatorCacheAutoConfiguration {
//
//    /**
//     * Esta clase se auto-configura cuando:
//     * 1. La librería share está en el classpath
//     * 2. La propiedad mediator.cache.enabled=true está configurada
//     *
//     * Proporciona automáticamente:
//     * - CachedMediatorImpl como implementación principal del IMediator
//     * - MediatorCacheCleanupService para limpieza programada
//     * - MediatorCacheController para monitoreo via REST API
//     * - Configuración de scheduling habilitada
//     */
//}
