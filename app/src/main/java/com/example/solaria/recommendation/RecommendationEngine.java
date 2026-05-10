package com.example.solaria.recommendation;

import com.example.solaria.model.UVData;

public class RecommendationEngine {

    public static UVData generateRecommendation(double uvIndex) {
        String riskLevel;
        String spf;
        String reapplyTime;

        if (uvIndex <= 2) {
            riskLevel = "Low";
            spf = "SPF 15 or optional protection";
            reapplyTime = "No frequent reapplication needed";
        }
        else if (uvIndex <= 5) {
            riskLevel = "Moderate";
            spf = "SPF 30";
            reapplyTime = "Reapply every 3 hours";
        }
        else if (uvIndex <= 7) {
            riskLevel = "High";
            spf = "SPF 50";
            reapplyTime = "Reapply every 2 hours";
        }
        else {
            riskLevel = "Very High / Extreme";
            spf = "SPF 50+";
            reapplyTime = "Reapply every 1 hour";
        }

        return new UVData(
                uvIndex,
                riskLevel,
                spf,
                reapplyTime
        );
    }
}