package com.example.solaria;
import android.content.Intent;
import android.view.View;
import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    // UI elemanları
    private TextView tvUVValue, tvRiskLevel, tvRiskSubtitle, tvGuidance, tvWeather, tvLastUpdated;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // UI elemanlarını bağla
        tvUVValue = findViewById(R.id.tvUVValue);
        tvRiskLevel = findViewById(R.id.tvRiskLevel);
        tvRiskSubtitle = findViewById(R.id.tvRiskSubtitle);
        tvGuidance = findViewById(R.id.tvGuidance);
        tvWeather = findViewById(R.id.tvWeather);
        tvLastUpdated = findViewById(R.id.tvLastUpdated);

        View btnSettings = findViewById(R.id.btnSettings);
        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

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

    // API'den gelen veriyi ekrana yaz
    private void updateUI(double uvIndex, double temperature, String weatherCondition,
                          String riskLevel, String message, String reapplyRule) {
        // UV değeri - 1 ondalık basamak
        tvUVValue.setText(String.format(Locale.US, "%.1f", uvIndex));

        // Risk seviyesi
        tvRiskLevel.setText(riskLevel.toUpperCase());

        // Mesaj (APPLY SUNSCREEN gibi)
        tvRiskSubtitle.setText(message.toUpperCase());

        // Reapply kuralı
        tvGuidance.setText(reapplyRule);

        // Hava durumu + sıcaklık
        String weatherText = capitalize(weatherCondition) + "\n" +
                String.format(Locale.US, "%.0f°C", temperature);
        tvWeather.setText(weatherText);

        // Son güncelleme saati
        String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
        tvLastUpdated.setText(time);
    }

    // Cache'den gelen veriyi ekrana yaz
    private void updateUIFromCache(UVWeatherCacheEntity cache) {
        tvUVValue.setText(String.format(Locale.US, "%.1f", cache.uvIndex));
        tvRiskLevel.setText(cache.riskLevel.toUpperCase());
        tvRiskSubtitle.setText(cache.message.toUpperCase());
        tvGuidance.setText(cache.reapplyRule);
        String weatherText = capitalize(cache.weatherCondition) + "\n" +
                String.format(Locale.US, "%.0f°C", cache.temperature);
        tvWeather.setText(weatherText);
        tvLastUpdated.setText(cache.lastUpdated != null ? cache.lastUpdated : "--:--");
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
                    runOnUiThread(() -> updateUIFromCache(cache));
                }
                @Override
                public void onCacheEmpty() {
                    runOnUiThread(() ->
                            android.util.Log.d("SOLARIA", "Offline, kayıtlı veri yok."));
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

                        runOnUiThread(() ->
                                updateUI(uvIndex, temperature, weatherCondition,
                                        riskLevel, message, reapplyRule));
                    }
                    @Override
                    public void onFailure(String errorMessage) {
                        android.util.Log.e("SOLARIA", "API hatası: " + errorMessage);
                        cacheManager.loadCachedUVData(new CacheManager.CacheReadCallback() {
                            @Override
                            public void onCacheLoaded(UVWeatherCacheEntity cache) {
                                runOnUiThread(() -> updateUIFromCache(cache));
                            }
                            @Override
                            public void onCacheEmpty() {
                                runOnUiThread(() ->
                                        android.util.Log.d("SOLARIA", "Hata ve kayıtlı veri yok."));
                            }
                        });
                    }
                });
            }
            @Override
            public void onLocationError(String errorMessage) {
                android.util.Log.e("SOLARIA", "Konum hatası: " + errorMessage);
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

    private String capitalize(String text) {
        if (text == null || text.isEmpty()) return text;
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }
}