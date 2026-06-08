package com.example.solaria;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.content.SharedPreferences;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.content.ContextCompat;
import android.widget.LinearLayout;
import android.content.Intent;
import android.widget.LinearLayout;

public class SettingsActivity extends Activity {

    private TextView btnBack;
    private Switch switchNotifications;
    private TextView txtLocationStatus;
    private LinearLayout cardHelp;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        LinearLayout helpCard = findViewById(R.id.helpCard);

        helpCard.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, HelpActivity.class);
            startActivity(intent);
        });




        btnBack = findViewById(R.id.btnBack);
        switchNotifications = findViewById(R.id.switchNotifications);
        SharedPreferences prefs = getSharedPreferences("SolariaPrefs", MODE_PRIVATE);
        boolean notificationsEnabled = prefs.getBoolean("notifications_enabled", false);
        switchNotifications.setChecked(notificationsEnabled);
        txtLocationStatus = findViewById(R.id.txtLocationStatus);
        helpCard = findViewById(R.id.helpCard);





        btnBack.setOnClickListener(v -> finish());

        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("notifications_enabled", isChecked).apply();

            if (isChecked) {
                Toast.makeText(this, "Notifications enabled", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Notifications disabled", Toast.LENGTH_SHORT).show();
            }
        });


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            txtLocationStatus.setText("Location Permission: Granted");

        } else {
            txtLocationStatus.setText("Location Permission: Denied");
        }

    }
}