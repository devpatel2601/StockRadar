package com.investresearch.service.ai;

import com.investresearch.config.CacheConfig;
import com.investresearch.model.SearchResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClaudeService {

    private static final int MAX_RESULTS_PER_CALL = 10;
    private static final int MAX_CONTENT_CHARS    = 350;
    private static final int MAX_RETRIES          = 3;
    private static final Pattern RETRY_AFTER_PATTERN =
            Pattern.compile("try again in (\\d+\\.?\\d*)s", Pattern.CASE_INSENSITIVE);

    private final ChatModel chatModel;
    private final CacheManager cacheManager;

    /**
     * Calls the AI model with caching. Cache key = SHA-256(systemPrompt + userContent).
     * Cache TTL is 24h (configured in CacheConfig). Returns the cached response if present.
     */
    public String analyze(String systemPrompt, String userContent) {
        String cacheKey = sha256(systemPrompt + "|||" + userContent);
        Cache cache = cacheManager.getCache(CacheConfig.AI_RESPONSES);

        if (cache != null) {
            Cache.ValueWrapper hit = cache.get(cacheKey);
            if (hit != null) {
                log.debug("AI response cache hit (key={}…)", cacheKey.substring(0, 8));
                return (String) hit.get();
            }
        }

        String result = callWithRetry(systemPrompt, userContent);
        if (cache != null) cache.put(cacheKey, result);
        return result;
    }

    public String synthesizePhase(String phaseName, String phaseInstructions, List<SearchResult> results) {
        String systemPrompt = """
                You are an expert investment research analyst specializing in Canadian markets.
                Synthesize the provided search results into structured, factual analysis.
                - Note the source URL and date of each piece of information.
                - Clearly distinguish between facts (earnings, prices) and opinions (analyst targets, forecasts).
                - If results contain mock/placeholder data, state that clearly and indicate what real data would show.
                - Be concise but thorough. Use headers and bullet points for readability.
                """;

        List<SearchResult> capped = results.stream().limit(MAX_RESULTS_PER_CALL).toList();
        if (results.size() > MAX_RESULTS_PER_CALL) {
            log.debug("Capped results for {} from {} to {}", phaseName, results.size(), MAX_RESULTS_PER_CALL);
        }

        String searchBlock = capped.stream()
                .map(r -> "Query: %s\nTitle: %s\nURL: %s\nContent: %s"
                        .formatted(r.getQuery(), r.getTitle(), r.getUrl(),
                                truncate(r.getFullContent(), MAX_CONTENT_CHARS)))
                .collect(Collectors.joining("\n\n---\n\n"));

        String userContent = """
                Phase: %s

                Instructions:
                %s

                Search Results:
                %s
                """.formatted(phaseName, phaseInstructions, searchBlock);

        return analyze(systemPrompt, userContent);
    }

    private String callWithRetry(String systemPrompt, String userContent) {
        int attempt = 0;
        while (true) {
            try {
                log.debug("Calling AI — attempt={}, contentLen={}", attempt + 1, userContent.length());
                Prompt prompt = new Prompt(List.of(
                        new SystemMessage(systemPrompt),
                        new UserMessage(userContent)
                ));
                ChatResponse response = chatModel.call(prompt);
                return response.getResult().getOutput().getText();
            } catch (Exception e) {
                attempt++;
                String msg = e.getMessage() != null ? e.getMessage() : "";
                boolean isRateLimit = msg.contains("429") || msg.contains("rate_limit_exceeded");
                if (!isRateLimit || attempt >= MAX_RETRIES) {
                    throw e;
                }
                long waitMs = parseWaitMs(msg);
                log.warn("Rate limit hit (attempt {}), backing off {}ms", attempt, waitMs);
                try {
                    Thread.sleep(waitMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted during rate-limit backoff", ie);
                }
            }
        }
    }

    private static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            return String.valueOf(input.hashCode());
        }
    }

    private long parseWaitMs(String errorMessage) {
        Matcher m = RETRY_AFTER_PATTERN.matcher(errorMessage);
        if (m.find()) {
            double seconds = Double.parseDouble(m.group(1));
            return (long)(seconds * 1000) + 2_000;
        }
        return 15_000;
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max) + "…";
    }
}
