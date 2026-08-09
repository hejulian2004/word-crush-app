package com.example.wordcrush.Database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.wordcrush.Database.GameRecord.GameRecordDao;
import com.example.wordcrush.Database.GameRecord.GameRecordEntity;
import com.example.wordcrush.Database.Word.WordDao;
import com.example.wordcrush.Database.Word.WordEntity;

@Database(entities = {WordEntity.class, GameRecordEntity.class, LearningMutationEntity.class}, version = 2, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase INSTANCE;
    public abstract WordDao wordDao();
    public abstract GameRecordDao gameRecordDao();
    public abstract LearningMutationDao learningMutationDao();
    public static AppDatabase getDatabase(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "word-crush")
                            .fallbackToDestructiveMigration(true)
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
