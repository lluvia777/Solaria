package com.example.solaria;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;

public class LocationHelper {

    public static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    public interface LocationCallback {
        void onLocationReceived(double latitude, double longitude);
        void onLocationError(String errorMessage);
    }

    private final Context context;
    private final FusedLocationProviderClient fusedClient;

    public LocationHelper(Context context) {
        this.context = context;
        this.fusedClient = LocationServices.getFusedLocationProviderClient(context);
    }

    public boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public void requestLocationPermission(Activity activity) {
        ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE);
    }

    public void getCurrentLocation(LocationCallback callback) {
        // İzin kontrolü - bu satır SecurityException'ı önler
        if (ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            callback.onLocationError("Konum izni verilmedi.");
            return;
        }

        CancellationTokenSource cts = new CancellationTokenSource();

        fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.getToken())
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        callback.onLocationReceived(location.getLatitude(), location.getLongitude());
                    } else {
                        // İzin tekrar kontrol et
                        if (ActivityCompat.checkSelfPermission(context,
                                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                                && ActivityCompat.checkSelfPermission(context,
                                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            callback.onLocationError("Konum izni yok.");
                            return;
                        }
                        fusedClient.getLastLocation()
                                .addOnSuccessListener(lastLocation -> {
                                    if (lastLocation != null) {
                                        callback.onLocationReceived(
                                                lastLocation.getLatitude(),
                                                lastLocation.getLongitude());
                                    } else {
                                        callback.onLocationError("Konum alınamadı. GPS açık mı?");
                                    }
                                })
                                .addOnFailureListener(e ->
                                        callback.onLocationError("Konum hatası: " + e.getMessage()));
                    }
                })
                .addOnFailureListener(e ->
                        callback.onLocationError("Konum hatası: " + e.getMessage()));
    }
}