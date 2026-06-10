package com.example.solaria;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "uv_weather_cache")
public class UVWeatherCacheEntity {


        @PrimaryKey
        public int cacheId = 1;

        public double uvIndex;
        public String riskLevel;
        public String message;
        public String reapplyRule;
        public double temperature;
        public String weatherCondition;
        public String locationName;
        public String lastUpdated;
    }

