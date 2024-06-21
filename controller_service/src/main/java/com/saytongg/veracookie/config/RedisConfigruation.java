package com.saytongg.veracookie.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration.JedisClientConfigurationBuilder;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.GenericToStringSerializer;

@Configuration
@ComponentScan("com.saytongg.veracookie.entity")
@EnableRedisRepositories(basePackages = "com.saytongg.veracookie.repository")
public class RedisConfigruation {

    @Value( "${redis.host}" )
    private String redisHostname;

    @Value( "${redis.port}" )
    private int redisPort;

    @Value( "${redis.password}" )
    private String redisPassword;

    @Value( "${redis.database}" )
    private int redisDatabase;

    @Value( "${redis.timeout}" )
    private long redisTimeout;

    @Bean
    public JedisConnectionFactory jedisConnectionFactory() {
        final RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
        redisStandaloneConfiguration.setHostName(redisHostname);
        redisStandaloneConfiguration.setPort(redisPort);
        redisStandaloneConfiguration.setPassword(redisPassword);
        redisStandaloneConfiguration.setDatabase(redisDatabase);

        final Duration timeout = Duration.ofSeconds(redisTimeout);
        final JedisClientConfigurationBuilder jedisClientConfiguration = JedisClientConfiguration.builder();
        jedisClientConfiguration.connectTimeout(timeout); // 60s connection timeout
        
        return new JedisConnectionFactory(redisStandaloneConfiguration, jedisClientConfiguration.build());
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        final RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(jedisConnectionFactory());
        template.setValueSerializer(new GenericToStringSerializer<Object>(Object.class));

        return template;
    }

}
