package com.example.solaria;

import android.os.Bundle;
import android.content.pm.PackageManager;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        CacheManager cacheManager = new CacheManager(this);
        NetworkHelper networkHelper = new NetworkHelper(this);
        LocationHelper locationHelper = new LocationHelper(this);
        ApiService apiService = new ApiService();

        loadData(locationHelper, networkHelper, apiService, cacheManager);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    private void loadData(LocationHelper locationHelper, NetworkHelper networkHelper,
                          ApiService apiService, CacheManager cacheManager) {

        if (!locationHelper.hasLocationPermission()) {
            locationHelper.requestLocationPermission(this);
            cacheManager.setLocationPermissionStatus("DENIED");
            return;
        }

        cacheManager.setLocationPermissionStatus("GRANTED");

        if (!networkHelper.isNetworkAvailable()) {
            cacheManager.loadCachedUVData(new CacheManager.CacheReadCallback() {
                @Override
                public void onCacheLoaded(UVWeatherCacheEntity cache) {
                    runOnUiThread(() -> {
                        android.util.Log.d("SOLARIA", "Offline - UV: " + cache.uvIndex);
                    });
                }
                @Override
                public void onCacheEmpty() {
                    runOnUiThread(() ->
                            android.util.Log.d("SOLARIA", "You are offline and no saved data was found."));
                }
            });
            return;
        }

        locationHelper.getCurrentLocation(new LocationHelper.LocationCallback() {
            @Override
            public void onLocationReceived(double lat, double lon) {
                apiService.fetchUVData(lat, lon, new ApiService.ApiCallback() {
                    @Override
                    public void onSuccess(double uvIndex, double temperature,
                                          String weatherCondition, String locationName) {
                        String riskLevel = getRiskLevel(uvIndex);
                        String message = getMessage(uvIndex);
                        String reapplyRule = getReapplyRule(uvIndex);

                        cacheManager.saveUVData(uvIndex, temperature,
                                weatherCondition, locationName,
                                riskLevel, message, reapplyRule);

                        runOnUiThread(() -> {
                            android.util.Log.d("SOLARIA", "UV: " + uvIndex + " / " + riskLevel);
                        });
                    }
                    @Override
                    public void onFailure(String errorMessage) {
                        cacheManager.loadCachedUVData(new CacheManager.CacheReadCallback() {
                            @Override
                            public void onCacheLoaded(UVWeatherCacheEntity cache) {
                                runOnUiThread(() ->
                                        android.util.Log.d("SOLARIA", "Could not update. Showing saved data."));
                            }
                            @Override
                            public void onCacheEmpty() {
                                runOnUiThread(() ->
                                        android.util.Log.d("SOLARIA", "Something went wrong and no saved data was found."));
                            }
                        });
                    }
                });
            }
            @Override
            public void onLocationError(String errorMessage) {
                runOnUiThread(() ->
                        android.util.Log.d("SOLARIA", "Location error: " + errorMessage));
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LocationHelper.LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                recreate();
            }
        }
    }

    private String getRiskLevel(double uv) {
        if (uv < 3) return "Low";
        else if (uv < 6) return "Moderate";
        else if (uv < 8) return "High";
        else if (uv < 11) return "Very High";
        else return "Extreme";
    }

    private String getMessage(double uv) {
        if (uv < 3) return "Low UV risk.";
        else if (uv < 6) return "Apply sunscreen.";
        else if (uv < 8) return "Wear sunscreen and a hat.";
        else if (uv < 11) return "Stay in the shade if possible.";
        else return "Avoid going outside!";
    }

    private String getReapplyRule(double uv) {
        if (uv < 3) return "No need to reapply.";
        else if (uv < 6) return "Reapply every 3–4 hours.";
        else if (uv < 9) return "Reapply every 2 hours.";
        else return "Start the reapplication timer.";
    }
}