package com.kynsof.share.core.infrastructure.redis;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

@Configuration
public class RedisConfig {

    private static final Logger logger = LoggerFactory.getLogger(RedisConfig.class);

    @Value("${spring.data.redis.host:localhost}")
    private String redisAddress;

    @Value("${spring.data.redis.port:6379}")
    private Integer redisPort;

    @Value("${spring.data.redis.username:}")
    private String redisUsername;

    @Value("${spring.data.redis.password:}")
    private String redisPassword;

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration(redisAddress, redisPort);

        if (!redisUsername.isEmpty()) {
            redisConfig.setUsername(redisUsername);
        }
        if (!redisPassword.isEmpty()) {
            redisConfig.setPassword(redisPassword);
        }
     //   redisConfig.setUsername("redis");
//        redisConfig.setPassword("IdoHj0o1oe");
        LettuceConnectionFactory factory = new LettuceConnectionFactory(redisConfig);
        factory.afterPropertiesSet();

        // Prueba de conexión
        try (RedisConnection connection = factory.getConnection()) {
            String pingResponse = connection.ping();
            logger.info("Redis connection established with address: {}:{} and username: {}. Ping response: {}",
                    redisAddress, redisPort, redisUsername.isEmpty() ? "none" : redisUsername, pingResponse);
        } catch (Exception e) {
            logger.error("Failed to connect to Redis at {}:{}", redisAddress, redisPort, e);
            logger.info("Redis connection established with address: {}:{} and username: {} ",
                    redisAddress, redisPort, redisUsername.isEmpty() ? "none" : redisUsername);
        }

        return factory;
    }

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://" + redisAddress + ":" + redisPort)
                .setUsername(redisUsername.isEmpty() ? null : redisUsername)
                .setPassword(redisPassword.isEmpty() ? null : redisPassword);

        RedissonClient client = Redisson.create(config);
        logger.info("Redisson client configured for Redis at {}:{}", redisAddress, redisPort);
        return client;
    }
}