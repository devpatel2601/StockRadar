package com.investresearch.service.search;

import com.investresearch.config.CacheConfig;
import com.investresearch.model.SearchResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Tavily Search integration. Activate by setting:
 *   search.provider=tavily
 *   TAVILY_API_KEY=<your-key>
 *
 * Results are cached in-memory (Caffeine) for 8 hours keyed by query string.
 * This is done manually in search() rather than via @Cacheable to avoid
 * Spring AOP self-invocation issues when searchAll() calls search() internally.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "search.provider", havingValue = "tavily")
public class TavilySearchService implements SearchService {

    private static final String TAVILY_URL = "https://api.tavily.com/search";

    @Value("${search.tavily.api-key}")
    private String apiKey;

    @Value("${search.max-results:5}")
    private int maxResults;

    private final WebClient.Builder webClientBuilder;
    private final CacheManager cacheManager;

    @Override
    @SuppressWarnings("unchecked")
    public List<SearchResult> search(String query) {
        Cache cache = cacheManager.getCache(CacheConfig.SEARCH_RESULTS);
        if (cache != null) {
            Cache.ValueWrapper hit = cache.get(query);
            if (hit != null) {
                log.debug("[TavilySearch] cache hit for '{}'", query);
                return (List<SearchResult>) hit.get();
            }
        }

        log.debug("[TavilySearch] querying Tavily: '{}'", query);
        try {
            Map<String, Object> requestBody = Map.of(
                    "api_key", apiKey,
                    "query", query,
                    "max_results", maxResults,
                    "include_raw_content", false
            );

            Map<String, Object> response = webClientBuilder.build()
                    .post()
                    .uri(TAVILY_URL)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response == null || !response.containsKey("results")) {
                return List.of();
            }

            List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("results");
            List<SearchResult> parsed = results.stream()
                    .map(r -> SearchResult.builder()
                            .query(query)
                            .title((String) r.getOrDefault("title", ""))
                            .snippet((String) r.getOrDefault("content", ""))
                            .url((String) r.getOrDefault("url", ""))
                            .fullContent((String) r.getOrDefault("content", ""))
                            .retrievedAt(LocalDateTime.now())
                            .build())
                    .toList();

            if (cache != null) cache.put(query, parsed);
            return parsed;

        } catch (Exception e) {
            log.error("Tavily search failed for query '{}': {}", query, e.getMessage());
            return List.of();
        }
    }

    @Override
    public List<SearchResult> searchAll(List<String> queries) {
        return queries.stream()
                .flatMap(q -> search(q).stream())
                .toList();
    }
}
