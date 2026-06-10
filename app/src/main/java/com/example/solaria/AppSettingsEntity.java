package com.example.solaria;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "app_settings")
public class AppSettingsEntity {


        @PrimaryKey
        public int settingsId = 1;
        public boolean notificationsEnabled = false;
        public String locationPermissionStatus = "UNKNOWN";
    }

