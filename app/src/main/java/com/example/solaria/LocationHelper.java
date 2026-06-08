package com.example.solaria;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
public class LocationHelper {

        private Activity activity;
        private FusedLocationProviderClient fusedLocationClient;

        public LocationHelper(Activity activity) {
            this.activity = activity;

            fusedLocationClient =
                    LocationServices.getFusedLocationProviderClient(activity);
        }

        public void getLocation(LocationCallback callback) {

            if (ActivityCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                        activity,
                        new String[]{
                                Manifest.permission.ACCESS_FINE_LOCATION
                        },
                        1);

                return;
            }

            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location -> {

                        if (location != null) {

                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();

                            callback.onLocationReceived(
                                    latitude,
                                    longitude
                            );
                        }
                    });
        }

        public interface LocationCallback {
            void onLocationReceived(double lat, double lon);
        }
    }

