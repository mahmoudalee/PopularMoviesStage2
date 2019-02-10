package com.example.dell.popularmoviesstage2.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {MovieEntry.class},version = 1,exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static final Object object = new Object();
    private static final String DATABASE_NAME = "favoriteMovies";
    private static AppDatabase sInstance;

    public static AppDatabase getsInstance(Context context){
        if (sInstance==null){
            synchronized (object){
              sInstance =  Room.databaseBuilder(context.getApplicationContext(),
                        AppDatabase.class,AppDatabase.DATABASE_NAME)
                      .build();
            }
        }
        return sInstance;
    }

    public abstract MovieDao movieDao();
}
