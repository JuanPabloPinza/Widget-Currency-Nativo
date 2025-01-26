package com.example.wdgt.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface CurrencyApi {
    @GET("latest")
    Call<ExchangeRatesResponse> getExchangeRates(
            @Query("apikey") String apiKey,
            @Query("base_currency") String baseCurrency,
            @Query("currencies") String targetCurrencies
    );
}