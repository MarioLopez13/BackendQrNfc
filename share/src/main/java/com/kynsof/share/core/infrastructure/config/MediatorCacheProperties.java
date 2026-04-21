//package com.kynsof.share.core.infrastructure.config;
//
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
//import org.springframework.boot.context.properties.ConfigurationProperties;
//import org.springframework.context.annotation.Configuration;
//
///**
// * Propiedades de configuración para el cache del Mediator
// * Se aplica automáticamente a todos los microservicios que usen la librería share
// */
//@Configuration
//@ConfigurationProperties(prefix = "mediator.cache")
//@ConditionalOnProperty(name = "mediator.cache.enabled", havingValue = "true", matchIfMissing = false)
//public class MediatorCacheProperties {
//
//    /**
//     * Habilitar el cache del mediator
//     */
//    private boolean enabled = false;
//
//    /**
//     * Configuración de limpieza automática
//     */
//    private Cleanup cleanup = new Cleanup();
//
//    public boolean isEnabled() {
//        return enabled;
//    }
//
//    public void setEnabled(boolean enabled) {
//        this.enabled = enabled;
//    }
//
//    public Cleanup getCleanup() {
//        return cleanup;
//    }
//
//    public void setCleanup(Cleanup cleanup) {
//        this.cleanup = cleanup;
//    }
//
//    public static class Cleanup {
//        /**
//         * Habilitar limpieza automática
//         */
//        private boolean enabled = true;
//
//        /**
//         * Intervalo de limpieza ligera en milisegundos (default: 6 horas)
//         */
//        private long interval = 21600000L;
//
//        /**
//         * Cron expression para limpieza completa diaria (default: 3:00 AM)
//         */
//        private String dailyCron = "0 0 3 * * *";
//
//        public boolean isEnabled() {
//            return enabled;
//        }
//
//        public void setEnabled(boolean enabled) {
//            this.enabled = enabled;
//        }
//
//        public long getInterval() {
//            return interval;
//        }
//
//        public void setInterval(long interval) {
//            this.interval = interval;
//        }
//
//        public String getDailyCron() {
//            return dailyCron;
//        }
//
//        public void setDailyCron(String dailyCron) {
//            this.dailyCron = dailyCron;
//        }
//    }
//}
