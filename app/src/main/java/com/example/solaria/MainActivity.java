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
                        // UI arkadaşı buraya ekrana yazdırma kodunu ekleyecek
                        android.util.Log.d("SOLARIA", "Offline - UV: " + cache.uvIndex);
                    });
                }
                @Override
                public void onCacheEmpty() {
                    runOnUiThread(() ->
                            android.util.Log.d("SOLARIA", "Offline ve cache boş"));
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
                            // UI arkadaşı buraya ekrana yazdırma kodunu ekleyecek
                            android.util.Log.d("SOLARIA", "UV: " + uvIndex + " / " + riskLevel);
                        });
                    }
                    @Override
                    public void onFailure(String errorMessage) {
                        cacheManager.loadCachedUVData(new CacheManager.CacheReadCallback() {
                            @Override
                            public void onCacheLoaded(UVWeatherCacheEntity cache) {
                                runOnUiThread(() ->
                                        android.util.Log.d("SOLARIA", "API hata, cache gösteriliyor"));
                            }
                            @Override
                            public void onCacheEmpty() {
                                runOnUiThread(() ->
                                        android.util.Log.d("SOLARIA", "API hata ve cache yok"));
                            }
                        });
                    }
                });
            }
            @Override
            public void onLocationError(String errorMessage) {
                runOnUiThread(() ->
                        android.util.Log.d("SOLARIA", "Konum hatası: " + errorMessage));
            }
        });
    }

    // İzin sonucu
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LocationHelper.LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                recreate(); // Activity'yi yeniden başlat
            }
        }
    }

    // RecommendationEngine - UV'ye göre hesaplamalar
    private String getRiskLevel(double uv) {
        if (uv < 3) return "Low";
        else if (uv < 6) return "Moderate";
        else if (uv < 8) return "High";
        else if (uv < 11) return "Very High";
        else return "Extreme";
    }

    private String getMessage(double uv) {
        if (uv < 3) return "UV riski düşük.";
        else if (uv < 6) return "Güneş kremi sür.";
        else if (uv < 8) return "Güneş kremi ve şapka kullan.";
        else if (uv < 11) return "Mümkünse gölgede kal.";
        else return "Dışarı çıkmaktan kaçın!";
    }

    private String getReapplyRule(double uv) {
        if (uv < 3) return "Tekrar uygulama gerekmez.";
        else if (uv < 6) return "Her 3-4 saatte bir tekrar uygula.";
        else if (uv < 9) return "Her 2 saatte bir tekrar uygula.";
        else return "Uygulama zamanlayıcısını başlat.";
    }
}