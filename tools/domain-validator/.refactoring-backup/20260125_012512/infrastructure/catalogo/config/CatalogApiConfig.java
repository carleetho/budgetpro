package com.budgetpro.infrastructure.catalogo.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Objects;

/**
 * Configuración del cliente HTTP y caches de catálogo.
 */
@Configuration
@EnableCaching
public class CatalogApiConfig {

    @Bean
    @ConditionalOnProperty(name = "catalog.provider", havingValue = "capeco")
    public RestTemplate catalogRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(5000);
        return new RestTemplate(factory);
    }

    @Bean(name = "catalogCaffeineCacheManager")
    @Primary
    public CacheManager catalogCaffeineCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
                "catalog-recursos-l1",
                "catalog-apus-l1",
                "catalog-recursos-search-l1",
                "catalog-recurso-active-l1"
        );
        cacheManager.setCaffeine(Objects.requireNonNull(Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(Objects.requireNonNull(Duration.ofHours(1), "TTL no puede ser nulo"))
                .recordStats(), "Caffeine builder no puede ser nulo"));
        return cacheManager;
    }

    @Bean(name = "catalogRedisCacheManager")
    public CacheManager catalogRedisCacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Objects.requireNonNull(Duration.ofHours(24), "TTL no puede ser nulo"))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                        new GenericJackson2JsonRedisSerializer()));
        return RedisCacheManager.builder(Objects.requireNonNull(connectionFactory, "RedisConnectionFactory no puede ser nulo"))
                .cacheDefaults(config)
                .build();
    }
}
