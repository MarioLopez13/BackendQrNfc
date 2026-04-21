//package com.kynsof.share.core.domain.kafka.config;
//
//import org.apache.kafka.clients.CommonClientConfigs;
//import org.apache.kafka.clients.producer.ProducerConfig;
//import org.apache.kafka.common.config.SaslConfigs;
//import org.apache.kafka.common.serialization.StringSerializer;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.kafka.core.DefaultKafkaProducerFactory;
//import org.springframework.kafka.core.KafkaTemplate;
//import org.springframework.kafka.core.ProducerFactory;
//
//import java.util.HashMap;
//import java.util.Map;
//
//@Configuration
//public class KafkaProducerConfig {
//
//    @Value("${KAFKA_BOOTSTRAP_ADDRESS:localhost:29092}")
//    private String bootstrapAddress;
//
//    @Value("${KAFKA_SASL_USERNAME:}")
//    private String saslUsername;
//
//    @Value("${KAFKA_SASL_PASSWORD:}")
//    private String saslPassword;
//
//    @Bean
//    public ProducerFactory<String, String> producerFactory() {
//        Map<String, Object> configProps = createBaseProps();
//        if (!saslUsername.isEmpty() && !saslPassword.isEmpty()) {
//            configProps.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_PLAINTEXT");
//            configProps.put(SaslConfigs.SASL_MECHANISM, "PLAIN");
//            configProps.put(SaslConfigs.SASL_JAAS_CONFIG,
//                    String.format("org.apache.kafka.common.security.plain.PlainLoginModule required username=\"%s\" password=\"%s\";",
//                            saslUsername, saslPassword));
//        }
//        return new DefaultKafkaProducerFactory<>(configProps);
//    }
//
//    private Map<String, Object> createBaseProps() {
//        Map<String, Object> configProps = new HashMap<>();
//        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
//        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
//        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
//        return configProps;
//    }
//
//    @Bean
//    public KafkaTemplate<String, String> kafkaTemplate() {
//        return new KafkaTemplate<>(producerFactory());
//    }
//}