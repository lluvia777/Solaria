package com.example.solaria;
import android.content.Context;
import android.os.AsyncTask;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
public class CacheManager {


        public interface CacheReadCallback {
            void onCacheLoaded(UVWeatherCacheEntity cache);
            void onCacheEmpty();
        }

        public interface SettingsReadCallback {
            void onSettingsLoaded(AppSettingsEntity settings);
        }

        private final RoomDatabaseClient db;

        public CacheManager(Context context) {
            this.db = RoomDatabaseClient.getInstance(context);
        }

        public void saveUVData(double uvIndex, double temperature,
                               String weatherCondition, String locationName,
                               String riskLevel, String message, String reapplyRule) {

            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    UVWeatherCacheEntity entity = new UVWeatherCacheEntity();
                    entity.cacheId = 1;
                    entity.uvIndex = uvIndex;
                    entity.temperature = temperature;
                    entity.weatherCondition = weatherCondition;
                    entity.locationName = locationName;
                    entity.riskLevel = riskLevel;
                    entity.message = message;
                    entity.reapplyRule = reapplyRule;
                    entity.lastUpdated = getCurrentTime();

                    db.uvWeatherCacheDao().insertOrUpdate(entity);
                    return null;
                }
            }.execute();
        }

        public void loadCachedUVData(CacheReadCallback callback) {
            new AsyncTask<Void, Void, UVWeatherCacheEntity>() {
                @Override
                protected UVWeatherCacheEntity doInBackground(Void... voids) {
                    return db.uvWeatherCacheDao().getLatestCache();
                }

                @Override
                protected void onPostExecute(UVWeatherCacheEntity result) {
                    if (result != null) {
                        callback.onCacheLoaded(result);
                    } else {
                        callback.onCacheEmpty();
                    }
                }
            }.execute();
        }

        public void hasCachedData(CacheReadCallback callback) {
            loadCachedUVData(callback);
        }

        public void saveSettings(boolean notificationsEnabled, String locationPermissionStatus) {
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    AppSettingsEntity entity = new AppSettingsEntity();
                    entity.settingsId = 1;
                    entity.notificationsEnabled = notificationsEnabled;
                    entity.locationPermissionStatus = locationPermissionStatus;
                    db.appSettingsDao().insertOrUpdate(entity);
                    return null;
                }
            }.execute();
        }
        public void setNotificationsEnabled(boolean enabled) {
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    AppSettingsEntity existing = db.appSettingsDao().getSettings();
                    if (existing == null) {
                        AppSettingsEntity entity = new AppSettingsEntity();
                        entity.notificationsEnabled = enabled;
                        db.appSettingsDao().insertOrUpdate(entity);
                    } else {
                        db.appSettingsDao().setNotificationsEnabled(enabled);
                    }
                    return null;
                }
            }.execute();
        }
        public void setLocationPermissionStatus(String status) {
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    AppSettingsEntity existing = db.appSettingsDao().getSettings();
                    if (existing == null) {
                        AppSettingsEntity entity = new AppSettingsEntity();
                        entity.locationPermissionStatus = status;
                        db.appSettingsDao().insertOrUpdate(entity);
                    } else {
                        db.appSettingsDao().setLocationPermissionStatus(status);
                    }
                    return null;
                }
            }.execute();
        }

        public void loadSettings(SettingsReadCallback callback) {
            new AsyncTask<Void, Void, AppSettingsEntity>() {
                @Override
                protected AppSettingsEntity doInBackground(Void... voids) {
                    return db.appSettingsDao().getSettings();
                }

                @Override
                protected void onPostExecute(AppSettingsEntity result) {
                    if (result == null) {
                        AppSettingsEntity defaults = new AppSettingsEntity();
                        callback.onSettingsLoaded(defaults);
                    } else {
                        callback.onSettingsLoaded(result);
                    }
                }
            }.execute();
        }
        private String getCurrentTime() {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            return sdf.format(new Date());
        }
    }

