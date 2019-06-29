package com.kai_jan_57.opendsbmobile.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.kai_jan_57.opendsbmobile.database.converters.DateConverter;

@Database(entities = {
        Login.class,
        Node.class
}, version = 16, exportSchema = false)
@TypeConverters(DateConverter.class)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase sInstance;

    public abstract Login.Dao getLoginDao();
    public abstract Node.Dao getNodeDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (sInstance == null) {
            sInstance = Room
                    .databaseBuilder(context.getApplicationContext(), AppDatabase.class, "app_database")
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return sInstance;
    }

// --Commented out by Inspection START (02.05.19 16:59):
//    public static void destroy() {
//        sInstance = null;
//    }
// --Commented out by Inspection STOP (02.05.19 16:59)

}
