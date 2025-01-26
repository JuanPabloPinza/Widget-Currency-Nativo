package com.example.wdgt.api;

import java.util.Map;

public class ExchangeRatesResponse {
    private Map<String, CurrencyData> data;

    public Map<String, CurrencyData> getData() {
        return data;
    }

    public static class CurrencyData {
        private double value;

        public double getValue() {
            return value;
        }
    }
}
