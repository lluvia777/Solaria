package com.example.solaria;

import android.os.AsyncTask;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ApiService {

    // ⚠️ BURAYA KENDİ API KEY'İNİ YAZ
    private static final String API_KEY = "BURAYA_API_KEY_YAZZ";

    public interface ApiCallback {
        void onSuccess(double uvIndex, double temperature, String weatherCondition, String locationName);
        void onFailure(String errorMessage);
    }

    public void fetchUVData(double latitude, double longitude, ApiCallback callback) {
        new FetchTask(latitude, longitude, callback).execute();
    }

    private static class FetchTask extends AsyncTask<Void, Void, String> {

        private final double lat, lon;
        private final ApiCallback callback;
        private String errorMsg = null;

        FetchTask(double lat, double lon, ApiCallback callback) {
            this.lat = lat;
            this.lon = lon;
            this.callback = callback;
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                // Hava durumu + UV verisi için Current Weather endpoint
                String weatherUrl = "https://api.openweathermap.org/data/2.5/weather"
                        + "?lat=" + lat
                        + "&lon=" + lon
                        + "&appid=" + API_KEY
                        + "&units=metric"
                        + "&lang=tr";

                String weatherResponse = makeRequest(weatherUrl);

                // UV Index için ayrı endpoint (One Call API 3.0 veya 2.5)
                String uvUrl = "https://api.openweathermap.org/data/2.5/uvi"
                        + "?lat=" + lat
                        + "&lon=" + lon
                        + "&appid=" + API_KEY;

                String uvResponse = makeRequest(uvUrl);

                // İki yanıtı birleştirip döndür
                return weatherResponse + "|||" + uvResponse;

            } catch (Exception e) {
                errorMsg = e.getMessage();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result == null || errorMsg != null) {
                callback.onFailure("API isteği başarısız: " + errorMsg);
                return;
            }

            try {
                String[] parts = result.split("\\|\\|\\|");
                JSONObject weatherJson = new JSONObject(parts[0]);
                JSONObject uvJson = new JSONObject(parts[1]);

                // UV Index
                double uvIndex = uvJson.getDouble("value");

                // Sıcaklık
                double temperature = weatherJson.getJSONObject("main").getDouble("temp");

                // Hava durumu açıklaması
                String weatherCondition = weatherJson
                        .getJSONArray("weather")
                        .getJSONObject(0)
                        .getString("description");

                // Şehir adı
                String locationName = weatherJson.getString("name");

                callback.onSuccess(uvIndex, temperature, weatherCondition, locationName);

            } catch (Exception e) {
                callback.onFailure("JSON parse hatası: " + e.getMessage());
            }
        }

        private String makeRequest(String urlString) throws Exception {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            int responseCode = conn.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new Exception("HTTP hata kodu: " + responseCode);
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
}