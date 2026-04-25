package com.investresearch.service.search;

import com.investresearch.model.SearchResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Stub implementation used until a real search provider (Tavily / Brave) is configured.
 * Set search.provider=tavily or search.provider=brave in application.properties to swap it out.
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "search.provider", havingValue = "mock", matchIfMissing = true)
public class MockSearchService implements SearchService {

    @Override
    public List<SearchResult> search(String query) {
        log.debug("[MockSearch] query='{}'", query);
        return List.of(SearchResult.builder()
                .query(query)
                .title("Mock Result — " + query)
                .snippet("Placeholder result. Configure Tavily or Brave Search for live data.")
                .url("https://example.com")
                .fullContent("""
                        This is a mock search result for: "%s"

                        To get real results:
                        1. Set search.provider=tavily in application.properties
                        2. Add TAVILY_API_KEY=<your-key> to your .env file

                        Tavily free tier: https://tavily.com
                        Brave Search API: https://api.search.brave.com
                        """.formatted(query))
                .retrievedAt(LocalDateTime.now())
                .build());
    }

    @Override
    public List<SearchResult> searchAll(List<String> queries) {
        return queries.stream()
                .flatMap(q -> search(q).stream())
                .toList();
    }
}
