package com.zhan_dui.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {MemoEntity.class}, version = 1, exportSchema = false)
@TypeConverters({}) // No converters needed for now
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase INSTANCE;

    public abstract MemoDao memoDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "EverMemo.db")
                            .fallbackToDestructiveMigration() // Since we have no migration yet
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}