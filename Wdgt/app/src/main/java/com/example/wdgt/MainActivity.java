package com.example.wdgt;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wdgt.api.ApiClient;
import com.example.wdgt.api.CurrencyApi;
import com.example.wdgt.api.ExchangeRatesResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;

public class MainActivity extends AppCompatActivity {
    String apiKey = BuildConfig.API_KEY;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView exchangeRateLabel = findViewById(R.id.exchangeRateLabel);
        Spinner baseCurrencySpinner = findViewById(R.id.baseCurrencySpinner);
        Spinner targetCurrencySpinner = findViewById(R.id.targetCurrencySpinner);
        Button refreshButton = findViewById(R.id.refreshButton);

        refreshButton.setOnClickListener(view -> {
            String baseCurrency = baseCurrencySpinner.getSelectedItem().toString();
            String targetCurrency = targetCurrencySpinner.getSelectedItem().toString();

            // Llama a la API para actualizar el tipo de cambio
            CurrencyApi api = ApiClient.getRetrofitInstance().create(CurrencyApi.class);
            Call<ExchangeRatesResponse> call = api.getExchangeRates(apiKey, baseCurrency, targetCurrency);

            call.enqueue(new Callback<ExchangeRatesResponse>() {
                @Override
                public void onResponse(Call<ExchangeRatesResponse> call, Response<ExchangeRatesResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        double rate = response.body().getData().get(targetCurrency).getValue();
                        exchangeRateLabel.setText(String.format("1 %s = %.2f %s", baseCurrency, rate, targetCurrency));

                        // Llama a la función para actualizar el widget
                        updateWidget(baseCurrency, targetCurrency, rate);
                    } else {
                        Toast.makeText(MainActivity.this, "Error al obtener el tipo de cambio", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ExchangeRatesResponse> call, Throwable t) {
                    Toast.makeText(MainActivity.this, "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void updateWidget(String baseCurrency, String targetCurrency, double rate) {
        Intent intent = new Intent(this, WidgetProvider.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);

        // Incluye los datos en el intent
        intent.putExtra("baseCurrency", baseCurrency);
        intent.putExtra("targetCurrency", targetCurrency);
        intent.putExtra("exchangeRate", rate);

        // Obtén los IDs de los widgets actuales
        int[] ids = AppWidgetManager.getInstance(this)
                .getAppWidgetIds(new ComponentName(this, WidgetProvider.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);

        sendBroadcast(intent); // Envía el broadcast al widget
    }
}

