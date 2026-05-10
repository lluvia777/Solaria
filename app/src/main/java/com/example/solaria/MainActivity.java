package com.example.solaria;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.solaria.model.UVData;
import com.example.solaria.recommendation.RecommendationEngine;
import com.example.solaria.storage.CacheManager;
import com.example.solaria.notification.NotificationHelper;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView textView = new TextView(this);
        textView.setTextSize(22);
        textView.setPadding(60, 60, 60, 60);
        setContentView(textView);

        double currentUV = 8.5;
        CacheManager.saveLastUVIndex(this, currentUV);

        UVData data = RecommendationEngine.generateRecommendation(currentUV);

        String displayMessage = "SOLARIA - FINAL SYSTEM TEST\n\n" +
                "Status: Data Processed ✅\n\n" +
                "Current UV Index: " + data.getUvIndex() + "\n" +
                "Risk Level: " + data.getRiskLevel() + "\n" +
                "Recommended SPF: " + data.getSpfRecommendation() + "\n" +
                "Reapplication: " + data.getReapplicationTime();

        textView.setText(displayMessage);

        String alertMessage = "Protect your skin! Use " + data.getSpfRecommendation();
        NotificationHelper.sendSunscreenReminder(this, alertMessage);
    }
}