package com.example.backend.model;

public class PriceResponse {
    private boolean success;
    private String message;
    private MarketData data;
    private String error;

    // Constructeurs
    public PriceResponse() {}

    public PriceResponse(boolean success, String message, MarketData data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public PriceResponse(boolean success, String error) {
        this.success = success;
        this.error = error;
    }

    // Getters et Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public MarketData getData() {
        return data;
    }

    public void setData(MarketData data) {
        this.data = data;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
