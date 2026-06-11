package com.example.solaria;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
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

    private TextView tvUVValue, tvRiskLevel, tvRiskSubtitle, tvGuidance, tvWeather, tvLastUpdated;
    private TextView tvReapplyRule;

    private CountDownTimer countDownTimer;
    private static final String PREFS_NAME = "solaria_timer";
    private static final String KEY_END_TIME = "timer_end_time";

    private double currentUvIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        tvUVValue = findViewById(R.id.tvUVValue);
        tvRiskLevel = findViewById(R.id.tvRiskLevel);
        tvRiskSubtitle = findViewById(R.id.tvRiskSubtitle);
        tvGuidance = findViewById(R.id.tvGuidance);
        tvWeather = findViewById(R.id.tvWeather);
        tvLastUpdated = findViewById(R.id.tvLastUpdated);
        tvReapplyRule = findViewById(R.id.tvReapplyRule);

        tvReapplyRule.setOnClickListener(v -> {
            if (isTimerRunning()) {
                stopTimer();
            } else {
                long duration = getTimerDuration(currentUvIndex);
                if (duration <= 0) {
                    // UV < 3, timer gerekmiyor
                    return;
                }
                startTimer(duration);
                NotificationHelper notificationHelper = new NotificationHelper(this);
                notificationHelper.scheduleNotification(currentUvIndex);
            }
        });

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
        resumeTimerIfRunning();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // SDS'e göre UV değerine göre timer süresi
    // UV < 3  → timer yok (0)
    // UV 3-6  → 3.5 saat
    // UV 6-8  → 2 saat
    // UV >= 8 → 2 saat (in-app protection timer)
    private long getTimerDuration(double uv) {
        if (uv < 3) return 0;
        else if (uv < 6) return (long) (3.5 * 60 * 60 * 1000);
        else return 2 * 60 * 60 * 1000L;
    }

    // Timer bitince gösterilecek varsayılan metin
    private String getDefaultReapplyText(double uv) {
        if (uv < 3) return "No need to reapply.";
        else if (uv < 6) return "Every 3-4h";
        else return "Every 2h";
    }

    private void startTimer(long durationMs) {
        long endTime = System.currentTimeMillis() + durationMs;
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putLong(KEY_END_TIME, endTime).apply();
        runTimer(durationMs);
    }

    private void resumeTimerIfRunning() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        long endTime = prefs.getLong(KEY_END_TIME, 0);
        long remaining = endTime - System.currentTimeMillis();
        if (remaining > 0) {
            runTimer(remaining);
        }
    }

    private void runTimer(long durationMs) {
        if (countDownTimer != null) countDownTimer.cancel();

        countDownTimer = new CountDownTimer(durationMs, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long hours = millisUntilFinished / (1000 * 60 * 60);
                long minutes = (millisUntilFinished % (1000 * 60 * 60)) / (1000 * 60);
                long seconds = (millisUntilFinished % (1000 * 60)) / 1000;
                tvReapplyRule.setText(String.format(Locale.getDefault(),
                        "%02d:%02d:%02d", hours, minutes, seconds));
            }

            @Override
            public void onFinish() {
                tvReapplyRule.setText(getDefaultReapplyText(currentUvIndex));
                clearTimerPrefs();
            }
        }.start();
    }

    private void stopTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
        tvReapplyRule.setText(getDefaultReapplyText(currentUvIndex));
        clearTimerPrefs();
        NotificationHelper notificationHelper = new NotificationHelper(this);
        notificationHelper.cancelNotification();
    }

    private boolean isTimerRunning() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        long endTime = prefs.getLong(KEY_END_TIME, 0);
        return endTime > System.currentTimeMillis();
    }

    private void clearTimerPrefs() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().remove(KEY_END_TIME).apply();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) countDownTimer.cancel();
    }

    private void updateUI(double uvIndex, double temperature, String weatherCondition,
                          String riskLevel, String message, String reapplyRule) {
        currentUvIndex = uvIndex;
        tvUVValue.setText(String.format(Locale.US, "%.1f", uvIndex));
        tvRiskLevel.setText(riskLevel.toUpperCase());
        tvRiskSubtitle.setText(message.toUpperCase());
        tvGuidance.setText(reapplyRule);
        String weatherText = capitalize(weatherCondition) + "\n" +
                String.format(Locale.US, "%.0f°C", temperature);
        tvWeather.setText(weatherText);
        String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
        tvLastUpdated.setText(time);

        // Timer çalışmıyorsa butonu güncelle
        if (!isTimerRunning()) {
            tvReapplyRule.setText(getDefaultReapplyText(uvIndex));
        }
    }

    private void updateUIFromCache(UVWeatherCacheEntity cache) {
        currentUvIndex = cache.uvIndex;
        tvUVValue.setText(String.format(Locale.US, "%.1f", cache.uvIndex));
        tvRiskLevel.setText(cache.riskLevel.toUpperCase());
        tvRiskSubtitle.setText(cache.message.toUpperCase());
        tvGuidance.setText(cache.reapplyRule);
        String weatherText = capitalize(cache.weatherCondition) + "\n" +
                String.format(Locale.US, "%.0f°C", cache.temperature);
        tvWeather.setText(weatherText);
        tvLastUpdated.setText(cache.lastUpdated != null ? cache.lastUpdated : "--:--");

        if (!isTimerRunning()) {
            tvReapplyRule.setText(getDefaultReapplyText(cache.uvIndex));
        }
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
                    android.util.Log.d("SOLARIA", "Offline, kayıtlı veri yok.");
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
                                android.util.Log.d("SOLARIA", "Hata ve kayıtlı veri yok.");
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