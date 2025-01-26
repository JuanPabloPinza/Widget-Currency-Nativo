package com.example.wdgt;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.StrictMode;
import android.widget.RemoteViews;
import android.util.Log;

import com.example.wdgt.api.ApiClient;
import com.example.wdgt.api.CurrencyApi;
import com.example.wdgt.api.ExchangeRatesResponse;

import retrofit2.Call;
import retrofit2.Response;

import android.content.ComponentName;


public class WidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // Permitir operaciones de red en el hilo principal (solo para pruebas)
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        for (int appWidgetId : appWidgetIds) {
            // Configurar la vista del widget
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

            // Llamar a la API para obtener las tasas de cambio
            CurrencyApi api = ApiClient.getRetrofitInstance().create(CurrencyApi.class);
            Call<ExchangeRatesResponse> call = api.getExchangeRates("cur_live_lIfKhIqVv8pLS3WbAHSztkQfzPw4gq9LI2G4utNU", "USD", "MXN");

            try {
                Response<ExchangeRatesResponse> response = call.execute();
                if (response.isSuccessful() && response.body() != null) {
                    double rate = response.body().getData().get("MXN").getValue();
                    views.setTextViewText(R.id.openApplication, String.format("1 USD = %.2f MXN", rate));
                }
            } catch (Exception e) {
                Log.e("WidgetProvider", "Error al obtener las tasas de cambio", e);
            }

            // Crear una acción para abrir la aplicación al hacer clic en el widget
            Intent intent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
            views.setOnClickPendingIntent(R.id.openApplication, pendingIntent);

            // Actualizar el widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (AppWidgetManager.ACTION_APPWIDGET_UPDATE.equals(intent.getAction())) {
            // Obtén los datos enviados desde la actividad
            String baseCurrency = intent.getStringExtra("baseCurrency");
            String targetCurrency = intent.getStringExtra("targetCurrency");
            double exchangeRate = intent.getDoubleExtra("exchangeRate", 0);

            // Actualiza los widgets
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            ComponentName thisWidget = new ComponentName(context, WidgetProvider.class);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

            for (int appWidgetId : appWidgetIds) {
                updateWidget(context, appWidgetManager, appWidgetId, baseCurrency, targetCurrency, exchangeRate);
            }
        }
    }
    private void updateWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId,
                              String baseCurrency, String targetCurrency, double exchangeRate) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        views.setTextViewText(R.id.openApplication,
                String.format("1 %s = %.2f %s", baseCurrency, exchangeRate, targetCurrency));

        // Configurar la acción para abrir la app
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.openApplication, pendingIntent);

        // Actualiza el widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }




}