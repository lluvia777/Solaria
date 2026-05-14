package com.example.solaria;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ApiService {

    private static final String API_KEY = BuildConfig.API_KEY;

    public static void getUVData(
            double lat,
            double lon,
            UVCallback callback) {

        OkHttpClient client = new OkHttpClient();

        String url =
                "https://api.openweathermap.org/data/2.5/uvi?lat="
                        + lat
                        + "&lon="
                        + lon
                        + "&appid="
                        + API_KEY;

        Request request =
                new Request.Builder()
                        .url(url)
                        .build();

        client.newCall(request)
                .enqueue(new Callback() {

                    @Override
                    public void onFailure(
                            Call call,
                            IOException e) {

                        callback.onError(
                                e.getMessage()
                        );
                    }

                    @Override
                    public void onResponse(
                            Call call,
                            Response response)
                            throws IOException {

                        String jsonData =
                                response.body().string();

                        try {

                            JSONObject obj =
                                    new JSONObject(jsonData);

                            double uv =
                                    obj.getDouble("value");

                            callback.onSuccess(uv);

                        } catch (Exception e) {

                            callback.onError(
                                    e.getMessage()
                            );
                        }
                    }
                });
    }

    public interface UVCallback {

        void onSuccess(double uv);

        void onError(String error);
    }
}