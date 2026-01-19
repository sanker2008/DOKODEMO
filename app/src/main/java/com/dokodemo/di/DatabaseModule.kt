package com.dokodemo.di

import android.content.Context
import androidx.room.Room
import com.dokodemo.data.AppDatabase
import com.dokodemo.data.dao.ServerDao
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
            "doko_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideServerDao(appDatabase: AppDatabase): ServerDao {
        return appDatabase.serverDao()
    }
}
