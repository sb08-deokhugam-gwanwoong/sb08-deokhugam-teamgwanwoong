package com.codeit.project.sb08deokhugamteamgwanwoong.config;

import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

  @Primary // 프로젝트 기본 캐시 매니저로 Redis 지정
  @Bean(name = "redisCacheManager")
  public RedisCacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {

    // 캐시 기본 설정
    RedisCacheConfiguration configuration = RedisCacheConfiguration.defaultCacheConfig()
        .disableCachingNullValues() // null 캐싱처리 X
        .entryTtl(Duration.ofDays(7)) // 7일동안 캐시 유지
        // Key를 문자열로 직렬화 (Redis CLI에서 보기 편하게 하기 위함)
        .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
        // Value는 JSON 형태로 직렬화해서 저장
        .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));

    return RedisCacheManager.builder(connectionFactory)
        .cacheDefaults(configuration)
        .build();
  }

}
