package com.example.solaria;

import android.os.Handler;
import android.os.Looper;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ApiService {

    private static final String API_KEY = "5dfbbb9b7db499ef1c6c8351e6be4428";

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface ApiCallback {
        void onSuccess(double uvIndex, double temperature, String weatherCondition, String locationName);
        void onFailure(String errorMessage);
    }

    public void fetchUVData(double latitude, double longitude, ApiCallback callback) {
        executor.execute(() -> {
            try {
                String weatherUrl = "https://api.openweathermap.org/data/2.5/weather"
                        + "?lat=" + latitude
                        + "&lon=" + longitude
                        + "&appid=" + API_KEY
                        + "&units=metric"
                        + "&lang=tr";

                String weatherResponse = makeRequest(weatherUrl);

                String uvUrl = "https://api.openweathermap.org/data/2.5/uvi"
                        + "?lat=" + latitude
                        + "&lon=" + longitude
                        + "&appid=" + API_KEY;

                String uvResponse = makeRequest(uvUrl);


                JSONObject weatherJson = new JSONObject(weatherResponse);
                JSONObject uvJson = new JSONObject(uvResponse);

                double uvIndex = uvJson.getDouble("value");
                double temperature = weatherJson.getJSONObject("main").getDouble("temp");
                String weatherCondition = weatherJson
                        .getJSONArray("weather")
                        .getJSONObject(0)
                        .getString("description");
                String locationName = weatherJson.getString("name");

                // Sonucu UI thread'e gönder
                mainHandler.post(() ->
                        callback.onSuccess(uvIndex, temperature, weatherCondition, locationName));

            } catch (Exception e) {
                mainHandler.post(() ->
                        callback.onFailure("API request failed: " + e.getMessage()));
            }
        });
    }

    private String makeRequest(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);

        int responseCode = conn.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new Exception("HTTP error code: " + responseCode);
        }

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        reader.close();
        conn.disconnect();

        return sb.toString();
    }
}