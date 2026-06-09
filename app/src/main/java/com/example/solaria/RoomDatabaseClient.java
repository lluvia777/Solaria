package com.example.solaria;
import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(
        entities = {UVWeatherCacheEntity.class, AppSettingsEntity.class},
        version = 1,
        exportSchema = false
)

    public abstract class RoomDatabaseClient extends RoomDatabase {

        private static RoomDatabaseClient instance;

        public abstract UVWeatherCacheDao uvWeatherCacheDao();
        public abstract AppSettingsDao appSettingsDao();

        // Singleton — uygulama boyunca tek instance
        public static synchronized RoomDatabaseClient getInstance(Context context) {
            if (instance == null) {
                instance = Room.databaseBuilder(
                                context.getApplicationContext(),
                                RoomDatabaseClient.class,
                                "solaria_database"
                        )
                        .fallbackToDestructiveMigration() // versiyon değişirse DB sıfırla
                        .build();
            }
            return instance;
        }
    }

