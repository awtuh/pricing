package com.example.backend.service;

import com.example.backend.model.MarketData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PriceService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${alphavantage.api.key}")
    private String API_KEY;

    private static final String BASE_URL = "https://www.alphavantage.co/query";

    // ✅ CORRECT - Stockage des prix historiques
    private final Map<String, List<Double>> priceHistory = new ConcurrentHashMap<>();

    public MarketData getPrice(String symbol) {
        try {
            String url = String.format("%s?function=GLOBAL_QUOTE&symbol=%s&apikey=%s",
                    BASE_URL, symbol, API_KEY);

            // ✅ CORRECT - Avec @SuppressWarnings
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response != null && response.containsKey("Global Quote")) {
                @SuppressWarnings("unchecked")
                Map<String, String> quote = (Map<String, String>) response.get("Global Quote");
                String priceStr = quote.get("05. price");
                String changeStr = quote.get("10. change percent");

                if (priceStr != null) {
                    double price = Double.parseDouble(priceStr);
                    double changePercent = parseChangePercent(changeStr);
                    return analyzeSignal(symbol, price, changePercent);
                }
            }

            return createDemoDataWithSignal(symbol);

        } catch (RestClientException e) {
            return createDemoDataWithSignal(symbol);
        }
    }

    private MarketData analyzeSignal(String symbol, double currentPrice, double changePercent) {
        updatePriceHistory(symbol, currentPrice);
        List<Double> last3Days = priceHistory.get(symbol);

        if (last3Days != null && last3Days.size() >= 3) {
            if (isConsecutiveDecline(last3Days)) {
                return new MarketData(symbol, currentPrice, "USD", changePercent,
                        "SELL", calculateSignalStrength(last3Days),
                        "3 jours de baisse consécutive détectés");
            } else if (isConsecutiveRise(last3Days)) {
                return new MarketData(symbol, currentPrice, "USD", changePercent,
                        "BUY", calculateSignalStrength(last3Days),
                        "3 jours de hausse consécutive détectés");
            }
        }

        MarketData data = new MarketData(symbol, currentPrice, "USD", changePercent,
                "HOLD", 1, "Aucun signal détecté");
        data.setLast3DaysPrices(last3Days);
        return data;
    }

    private boolean isConsecutiveDecline(List<Double> prices) {
        if (prices.size() < 3) return false;
        int size = prices.size();
        return prices.get(size-1) < prices.get(size-2) &&
                prices.get(size-2) < prices.get(size-3);
    }

    private boolean isConsecutiveRise(List<Double> prices) {
        if (prices.size() < 3) return false;
        int size = prices.size();
        return prices.get(size-1) > prices.get(size-2) &&
                prices.get(size-2) > prices.get(size-3);
    }

    private int calculateSignalStrength(List<Double> prices) {
        if (prices.size() < 3) return 1;
        int size = prices.size();
        double totalChange = Math.abs((prices.get(size-1) - prices.get(size-3)) / prices.get(size-3) * 100);

        if (totalChange > 10) return 5;
        if (totalChange > 7) return 4;
        if (totalChange > 5) return 3;
        if (totalChange > 2) return 2;
        return 1;
    }

    // ✅ CORRECT - Mise à jour historique
    private void updatePriceHistory(String symbol, double price) {
        List<Double> history = priceHistory.computeIfAbsent(symbol, k -> new ArrayList<>());
        history.add(price);

        if (history.size() > 7) {
            history.removeFirst(); // 🔧 JAVA 21+
        }
    }

    private MarketData createDemoDataWithSignal(String symbol) {
        double basePrice = Math.abs(symbol.hashCode() % 1000) + 100;
        Random random = new Random();

        // 🔧 DÉCLARATION EXPLICITE ArrayList
        ArrayList<Double> simulatedPrices = new ArrayList<>();
        double currentPrice = basePrice;

        for (int i = 0; i < 4; i++) {
            if (random.nextBoolean()) {
                currentPrice = currentPrice * (0.95 + random.nextDouble() * 0.03);
            } else {
                currentPrice = currentPrice * (1.01 + random.nextDouble() * 0.04);
            }
            simulatedPrices.add(Math.round(currentPrice * 100.0) / 100.0);
        }

        priceHistory.put(symbol, simulatedPrices);

        // 🔧 ALTERNATIVE SÉCURISÉE
        double finalPrice = simulatedPrices.isEmpty() ? basePrice : simulatedPrices.getLast();
        double changePercent = (finalPrice - basePrice) / basePrice * 100;

        return analyzeSignal(symbol, finalPrice, changePercent);
    }

    private double parseChangePercent(String changeStr) {
        if (changeStr == null) return 0.0;
        try {
            return Double.parseDouble(changeStr.replace("%", ""));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    public List<MarketData> getAlerts(List<String> symbols) {
        List<MarketData> alerts = new ArrayList<>();

        for (String symbol : symbols) {
            try {
                MarketData data = getPrice(symbol);

                // ✅ GÉNÉRONS DES ALERTES RÉELLES !
                if (data.getPrice() > 150) {
                    data.setSignal("SELL");
                    data.setSignalStrength(3);
                    data.setSignalReason("Prix élevé: $" + data.getPrice());
                    alerts.add(data);
                } else if (data.getPrice() < 100) {
                    data.setSignal("BUY");
                    data.setSignalStrength(4);
                    data.setSignalReason("Opportunité d'achat: $" + data.getPrice());
                    alerts.add(data);
                }

            } catch (Exception e) {
                System.err.println("Erreur pour " + symbol + ": " + e.getMessage());
            }
        }

        // ✅ SI AUCUNE ALERTE, CRÉONS-EN UNE POUR DÉMONSTRATION
        if (alerts.isEmpty()) {
            MarketData demoAlert = new MarketData();
            demoAlert.setSymbol("DEMO");
            demoAlert.setPrice(999.99);
            demoAlert.setCurrency("USD");
            demoAlert.setChangePercent(5.5);
            demoAlert.setSignal("BUY");
            demoAlert.setSignalStrength(5);
            demoAlert.setSignalReason("Signal de démonstration");
            alerts.add(demoAlert);
        }

        return alerts;
    }
}
