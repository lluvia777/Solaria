package com.example.solaria;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
    public interface AppSettingsDao {

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        void insertOrUpdate(AppSettingsEntity settings);

        @Query("SELECT * FROM app_settings WHERE settingsId = 1")
        AppSettingsEntity getSettings();


        @Query("UPDATE app_settings SET notificationsEnabled = :enabled WHERE settingsId = 1")
        void setNotificationsEnabled(boolean enabled);


        @Query("UPDATE app_settings SET locationPermissionStatus = :status WHERE settingsId = 1")
        void setLocationPermissionStatus(String status);
    }

