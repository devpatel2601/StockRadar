package com.investresearch.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    // Cache names referenced by TavilySearchService and ClaudeService
    public static final String SEARCH_RESULTS = "searchResults";
    public static final String AI_RESPONSES   = "aiResponses";

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager manager = new SimpleCacheManager();
        manager.setCaches(List.of(
            buildCache(SEARCH_RESULTS, 8,  500),  // 8h TTL — macro/smart-money queries stale-safe
            buildCache(AI_RESPONSES,  24, 200)    // 24h TTL — synthesis is expensive to re-run
        ));
        return manager;
    }

    private CaffeineCache buildCache(String name, long ttlHours, int maxSize) {
        return new CaffeineCache(name, Caffeine.newBuilder()
                .expireAfterWrite(ttlHours, TimeUnit.HOURS)
                .maximumSize(maxSize)
                .recordStats()
                .build());
    }
}
