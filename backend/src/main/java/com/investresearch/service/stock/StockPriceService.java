package com.investresearch.service.stock;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Fetches real-time stock prices from Yahoo Finance v8 chart endpoint.
 * No API key required. Fetches one ticker at a time (v8 chart API).
 */
@Slf4j
@Service
public class StockPriceService {

    private static final String YF_CHART_URL =
            "https://query2.finance.yahoo.com/v8/finance/chart/%s?interval=1d&range=1d&includePrePost=false";

    private final WebClient webClient = WebClient.builder()
            .defaultHeader("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36")
            .defaultHeader("Accept", "*/*")
            .defaultHeader("Accept-Language", "en-US,en;q=0.9")
            .defaultHeader("Origin", "https://finance.yahoo.com")
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

        List<StockPrice> prices = new ArrayList<>();
        for (String ticker : tickers) {
            prices.add(fetchOne(ticker));
        }
        return prices;
    }

    private StockPrice fetchOne(String ticker) {
        String url = YF_CHART_URL.formatted(ticker);
        try {
            Map<?, ?> body = webClient.get()
                    .uri(url)
                    .header("Referer", "https://finance.yahoo.com/quote/" + ticker)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (body == null) return notFound(ticker);

            Map<?, ?> chart  = (Map<?, ?>) body.get("chart");
            if (chart == null) return notFound(ticker);

            List<?> results = (List<?>) chart.get("result");
            if (results == null || results.isEmpty()) return notFound(ticker);

            Map<?, ?> meta = (Map<?, ?>) ((Map<?, ?>) results.get(0)).get("meta");
            if (meta == null) return notFound(ticker);

            double price       = num(meta, "regularMarketPrice");
            double prevClose   = num(meta, "chartPreviousClose");
            double change      = price - prevClose;
            double changePct   = prevClose != 0 ? (change / prevClose) * 100 : 0;
            String currency    = str(meta, "currency");
            String name        = str(meta, "longName");
            if (name.isEmpty()) name = str(meta, "shortName");

            log.debug("Yahoo Finance v8: {} = {} {}", ticker, currency, price);
            return new StockPrice(ticker, name, price, change, changePct, prevClose, currency, true);

        } catch (Exception e) {
            log.warn("Yahoo Finance price fetch failed for {}: {}", ticker, e.getMessage());
            return notFound(ticker);
        }
    }

    private StockPrice notFound(String ticker) {
        return new StockPrice(ticker, "", 0, 0, 0, 0, "", false);
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
