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
        // 1. Try the ticker exactly as given
        StockPrice result = fetchRaw(ticker, ticker);
        if (result.found()) return result;

        // 2. If it already has an exchange suffix, don't retry
        if (hasExchangeSuffix(ticker)) return result;

        // 3. Try with .TO suffix (TSX) — normalise share-class dots to hyphens first
        //    e.g. BAM.A -> BAM-A.TO,  ATD.B -> ATD-B.TO,  SU -> SU.TO
        String tsxSymbol = toTsxSymbol(ticker);
        StockPrice tsxResult = fetchRaw(tsxSymbol, ticker); // keep original ticker in response
        if (tsxResult.found()) return tsxResult;

        return result;
    }

    private StockPrice fetchRaw(String symbol, String originalTicker) {
        String url = YF_CHART_URL.formatted(symbol);
        try {
            Map<?, ?> body = webClient.get()
                    .uri(url)
                    .header("Referer", "https://finance.yahoo.com/quote/" + symbol)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (body == null) return notFound(originalTicker);

            Map<?, ?> chart = (Map<?, ?>) body.get("chart");
            if (chart == null) return notFound(originalTicker);

            List<?> results = (List<?>) chart.get("result");
            if (results == null || results.isEmpty()) return notFound(originalTicker);

            Map<?, ?> meta = (Map<?, ?>) ((Map<?, ?>) results.get(0)).get("meta");
            if (meta == null) return notFound(originalTicker);

            double price     = num(meta, "regularMarketPrice");
            double prevClose = num(meta, "chartPreviousClose");
            double change    = price - prevClose;
            double changePct = prevClose != 0 ? (change / prevClose) * 100 : 0;
            String currency  = str(meta, "currency");
            String name      = str(meta, "longName");
            if (name.isEmpty()) name = str(meta, "shortName");

            log.debug("Yahoo Finance v8: {} ({}) = {} {}", originalTicker, symbol, currency, price);
            return new StockPrice(originalTicker, name, price, change, changePct, prevClose, currency, true);

        } catch (Exception e) {
            log.warn("Yahoo Finance fetch failed for {} ({}): {}", originalTicker, symbol, e.getMessage());
            return notFound(originalTicker);
        }
    }

    // Returns true if ticker already has an exchange suffix like .TO, .V, .CN, .NE
    private boolean hasExchangeSuffix(String ticker) {
        return ticker.matches("(?i).*\\.(TO|V|CN|NE|TSX|NYSE|NASDAQ)$");
    }

    // Converts a plain or share-class ticker to TSX Yahoo Finance format:
    //   SU      -> SU.TO
    //   BAM.A   -> BAM-A.TO
    //   ATD.B   -> ATD-B.TO
    private String toTsxSymbol(String ticker) {
        // Share class: one letter after the last dot, e.g. BAM.A
        if (ticker.matches("[A-Z0-9]+(\\.[A-Z])$")) {
            return ticker.replace(".", "-") + ".TO";
        }
        return ticker + ".TO";
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
