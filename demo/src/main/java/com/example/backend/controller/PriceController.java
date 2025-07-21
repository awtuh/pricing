package com.example.backend.controller;

import com.example.backend.model.MarketData;
import com.example.backend.service.PriceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:4200")
public class PriceController {

    @Autowired
    private PriceService priceService;

    // ✅ ENDPOINT SIMPLE POUR UN SYMBOLE
    @GetMapping("/prices/{symbol}")
    public ResponseEntity<Map<String, Object>> getPrice(@PathVariable String symbol) {
        try {
            MarketData marketData = priceService.getPrice(symbol);
            Map<String, Object> priceData = new HashMap<>();

            priceData.put("symbol", marketData.getSymbol());
            priceData.put("price", marketData.getPrice());
            priceData.put("currency", marketData.getCurrency());
            priceData.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(priceData);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Prix non disponible");
            error.put("symbol", symbol);
            return ResponseEntity.ok(error);
        }
    }

    // ✅ ENDPOINT POUR LES ALERTES
    @GetMapping("/alerts")
    public ResponseEntity<List<MarketData>> getAlerts() {
        try {
            List<String> symbols = Arrays.asList("AAPL", "GOOGL", "MSFT", "TSLA", "AMZN");
            List<MarketData> alerts = priceService.getAlerts(symbols);
            return ResponseEntity.ok(alerts);
        } catch (Exception e) {
            return ResponseEntity.ok(new ArrayList<>());
        }
    }

    // ✅ ENDPOINT TEST SIMPLE
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("API fonctionne !");
    }
}
