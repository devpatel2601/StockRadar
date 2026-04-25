package com.investresearch.service.search;

import com.investresearch.model.SearchResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Tavily Search integration. Activate by setting:
 *   search.provider=tavily
 *   TAVILY_API_KEY=<your-key>
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

    @Override
    @SuppressWarnings("unchecked")
    public List<SearchResult> search(String query) {
        log.debug("[TavilySearch] query='{}'", query);
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
            return results.stream()
                    .map(r -> SearchResult.builder()
                            .query(query)
                            .title((String) r.getOrDefault("title", ""))
                            .snippet((String) r.getOrDefault("content", ""))
                            .url((String) r.getOrDefault("url", ""))
                            .fullContent((String) r.getOrDefault("content", ""))
                            .retrievedAt(LocalDateTime.now())
                            .build())
                    .toList();
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
