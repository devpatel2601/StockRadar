package com.investresearch.controller;

import com.investresearch.service.stock.StockPriceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
public class StockPriceController {

    private final StockPriceService stockPriceService;

    @GetMapping("/prices")
    public ResponseEntity<List<StockPriceService.StockPrice>> getPrices(
            @RequestParam String tickers) {
        List<String> tickerList = Arrays.stream(tickers.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
        return ResponseEntity.ok(stockPriceService.getPrices(tickerList));
    }
}
