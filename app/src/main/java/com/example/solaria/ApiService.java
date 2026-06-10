package com.example.solaria;

import android.os.AsyncTask;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class ApiService {


    private static final String API_KEY ="sk-ant-75e3f5ca82c85553174290f539f9682f";

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
                String weatherUrl = "https://api.openweathermap.org/data/2.5/weather"
                        + "?lat=" + lat
                        + "&lon=" + lon
                        + "&appid=" + API_KEY
                        + "&units=metric"
                        + "&lang=tr";

                String weatherResponse = makeRequest(weatherUrl);

                String uvUrl = "https://api.openweathermap.org/data/2.5/uvi"
                        + "?lat=" + lat
                        + "&lon=" + lon
                        + "&appid=" + API_KEY;

                String uvResponse = makeRequest(uvUrl);

                return weatherResponse + "|||" + uvResponse;

            } catch (Exception e) {
                errorMsg = e.getMessage();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result == null || errorMsg != null) {
                callback.onFailure("API request failed: " + errorMsg);
                return;
            }

            try {
                String[] parts = result.split("\\|\\|\\|");
                JSONObject weatherJson = new JSONObject(parts[0]);
                JSONObject uvJson = new JSONObject(parts[1]);

                double uvIndex = uvJson.getDouble("value");

                double temperature = weatherJson.getJSONObject("main").getDouble("temp");

                String weatherCondition = weatherJson
                        .getJSONArray("weather")
                        .getJSONObject(0)
                        .getString("description");

                String locationName = weatherJson.getString("name");

                callback.onSuccess(uvIndex, temperature, weatherCondition, locationName);

            } catch (Exception e) {
                callback.onFailure("JSON parse error: " + e.getMessage());
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
}