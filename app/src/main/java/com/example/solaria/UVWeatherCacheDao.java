package com.example.solaria;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
    public interface UVWeatherCacheDao {

        // Kaydet (varsa üzerine yaz)
        @Insert(onConflict = OnConflictStrategy.REPLACE)
        void insertOrUpdate(UVWeatherCacheEntity cache);

        // En son kaydı getir
        @Query("SELECT * FROM uv_weather_cache WHERE cacheId = 1")
        UVWeatherCacheEntity getLatestCache();

        // Cache var mı?
        @Query("SELECT COUNT(*) FROM uv_weather_cache")
        int getCacheCount();
    }

