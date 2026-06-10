package com.example.solaria;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
    public interface UVWeatherCacheDao {

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        void insertOrUpdate(UVWeatherCacheEntity cache);

        @Query("SELECT * FROM uv_weather_cache WHERE cacheId = 1")
        UVWeatherCacheEntity getLatestCache();

        @Query("SELECT COUNT(*) FROM uv_weather_cache")
        int getCacheCount();
    }

