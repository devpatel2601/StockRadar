package com.investresearch.service.stock;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class StockPriceService {

    private static final String YAHOO_URL =
            "https://query1.finance.yahoo.com/v7/finance/quote?symbols=%s&fields=regularMarketPrice,regularMarketChange,regularMarketChangePercent,regularMarketPreviousClose,currency,shortName";

    private final WebClient webClient = WebClient.builder()
            .defaultHeader("User-Agent", "Mozilla/5.0")
            .defaultHeader("Accept", "application/json")
            .build();

    public record StockPrice(
            String ticker,
            String name,
            double price,
            double change,
            double changePercent,
            double previousClose,
            String currency,
            boolean found
    ) {}

    public List<StockPrice> getPrices(List<String> tickers) {
        if (tickers == null || tickers.isEmpty()) return List.of();

        String symbols = String.join(",", tickers);
        String url = YAHOO_URL.formatted(symbols);

        try {
            Map<?, ?> body = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (body == null) return notFound(tickers);

            Map<?, ?> finance = (Map<?, ?>) body.get("quoteResponse");
            if (finance == null) return notFound(tickers);

            List<?> results = (List<?>) finance.get("result");
            if (results == null || results.isEmpty()) return notFound(tickers);

            List<StockPrice> prices = new ArrayList<>();
            for (Object item : results) {
                Map<?, ?> q = (Map<?, ?>) item;
                String symbol   = str(q, "symbol");
                String name     = str(q, "shortName");
                double price    = num(q, "regularMarketPrice");
                double change   = num(q, "regularMarketChange");
                double changePct = num(q, "regularMarketChangePercent");
                double prevClose = num(q, "regularMarketPreviousClose");
                String currency = str(q, "currency");
                prices.add(new StockPrice(symbol, name, price, change, changePct, prevClose, currency, true));
            }

            // add not-found entries for tickers missing from the response
            List<String> found = prices.stream().map(StockPrice::ticker).toList();
            for (String t : tickers) {
                if (found.stream().noneMatch(f -> f.equalsIgnoreCase(t))) {
                    prices.add(new StockPrice(t, "", 0, 0, 0, 0, "", false));
                }
            }

            log.debug("Yahoo Finance: fetched {} prices for {}", prices.stream().filter(StockPrice::found).count(), tickers);
            return prices;

        } catch (Exception e) {
            log.warn("Yahoo Finance price fetch failed for {}: {}", tickers, e.getMessage());
            return notFound(tickers);
        }
    }

    private List<StockPrice> notFound(List<String> tickers) {
        return tickers.stream()
                .map(t -> new StockPrice(t, "", 0, 0, 0, 0, "", false))
                .toList();
    }

    private String str(Map<?, ?> m, String key) {
        Object v = m.get(key);
        return v != null ? v.toString() : "";
    }

    private double num(Map<?, ?> m, String key) {
        Object v = m.get(key);
        if (v instanceof Number n) return n.doubleValue();
        return 0;
    }
}
