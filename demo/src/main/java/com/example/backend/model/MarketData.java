package com.example.backend.model;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Setter
@Getter
public class MarketData {
    private String symbol;
    private double price;
    private String currency;
    private double changePercent;
    private String signal;
    private int signalStrength;
    private String signalReason;
    private List<Double> last3DaysPrices;

    // CONSTRUCTEUR COMPLET
    public MarketData(String symbol, double price, String currency, double changePercent,
                      String signal, int signalStrength, String signalReason) {
        this.symbol = symbol;
        this.price = price;
        this.currency = currency;
        this.changePercent = changePercent;
        this.signal = signal;
        this.signalStrength = signalStrength;
        this.signalReason = signalReason;
    }

    // CONSTRUCTEUR SIMPLE
    public MarketData(String symbol, double price, String currency, double changePercent) {
        this(symbol, price, currency, changePercent, "HOLD", 1, "Aucun signal");
    }

    // CONSTRUCTEUR VIDE
    public MarketData() {}

    // ✅ SUPPRIMÉ LA MÉTHODE getTimestamp() PROBLÉMATIQUE
}