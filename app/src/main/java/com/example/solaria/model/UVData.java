package com.example.solaria.model;

/**
 * Data model for UV information and sunscreen recommendations.
 */
public class UVData {
    private double uvIndex;
    private String riskLevel;
    private String spfRecommendation;
    private String reapplicationTime;

    public UVData(double uvIndex,
                  String riskLevel,
                  String spfRecommendation,
                  String reapplicationTime) {

        this.uvIndex = uvIndex;
        this.riskLevel = riskLevel;
        this.spfRecommendation = spfRecommendation;
        this.reapplicationTime = reapplicationTime;
    }


    public double getUvIndex() {
        return uvIndex;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public String getSpfRecommendation() {
        return spfRecommendation;
    }

    public String getReapplicationTime() {
        return reapplicationTime;
    }
}