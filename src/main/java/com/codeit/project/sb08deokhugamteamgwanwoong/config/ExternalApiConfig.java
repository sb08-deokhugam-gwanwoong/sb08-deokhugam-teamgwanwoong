package com.codeit.project.sb08deokhugamteamgwanwoong.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableRetry
@EnableCaching
public class ExternalApiConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean(name = "caffeineCacheManager")
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();

        // 공통 설정
        cacheManager.setCaffeine(Caffeine.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)     // 10분 후 만료
            .maximumSize(100)                                       // 최대 100개 항목 저장
            .recordStats());

        // 인증번호용 캐시
        cacheManager.registerCustomCache("verificationCodes",
            Caffeine.newBuilder()
                .expireAfterWrite(3, TimeUnit.MINUTES)  // 3분 후 만료
                .maximumSize(50)                                    // 최대 50개 항목 저장
                .build());

        cacheManager.setCacheNames(Arrays.asList("bookMetadata", "ocrIsbn"));

        return cacheManager;
    }
}
