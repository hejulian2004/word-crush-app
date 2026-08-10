package com.example.wordcrush.Database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.wordcrush.Database.GameRecord.GameRecordDao
import com.example.wordcrush.Database.GameRecord.GameRecordEntity
import com.example.wordcrush.Database.Word.WordDao
import com.example.wordcrush.Database.Word.WordEntity

@Database(
    entities = [WordEntity::class, GameRecordEntity::class, LearningMutationEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun wordDao(): WordDao

    abstract fun gameRecordDao(): GameRecordDao

    abstract fun learningMutationDao(): LearningMutationDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        @JvmStatic
        fun getDatabase(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "word-crush"
                )
                    .fallbackToDestructiveMigration(true)
                    .fallbackToDestructiveMigrationOnDowngrade(true)
                    .build()
                    .also { instance = it }
            }
        }
    }
}
