package com.example.wordcrush.di

import android.content.Context
import androidx.room.Room
import com.example.wordcrush.Database.AppDatabase
import com.example.wordcrush.Database.GameRecord.GameRecordDao
import com.example.wordcrush.Database.Word.WordDao
import com.example.wordcrush.Database.LearningMutationDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "word-crush"
        )
            .fallbackToDestructiveMigration(true)
            .build()
    }

    @Provides
    @Singleton
    fun provideWordDao(database: AppDatabase): WordDao {
        return database.wordDao()
    }

    @Provides
    @Singleton
    fun provideGameRecordDao(database: AppDatabase): GameRecordDao {
        return database.gameRecordDao()
    }

    @Provides
    @Singleton
    fun provideLearningMutationDao(database: AppDatabase): LearningMutationDao {
        return database.learningMutationDao()
    }
}
