package com.dokodemo.di

import android.content.Context
import androidx.room.Room
import com.dokodemo.data.AppDatabase
import com.dokodemo.data.MIGRATION_1_2
import com.dokodemo.data.MIGRATION_2_3
import com.dokodemo.data.MIGRATION_3_4
import com.dokodemo.data.MIGRATION_4_5
import com.dokodemo.data.dao.GroupDao
import com.dokodemo.data.dao.ServerDao
import com.dokodemo.data.dao.SubscriptionDao
import com.dokodemo.data.dao.TrafficRecordDao
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
        )
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
            .build()
    }

    @Provides
    @Singleton
    fun provideServerDao(appDatabase: AppDatabase): ServerDao {
        return appDatabase.serverDao()
    }

    @Provides
    @Singleton
    fun provideSubscriptionDao(appDatabase: AppDatabase): SubscriptionDao {
        return appDatabase.subscriptionDao()
    }

    @Provides
    @Singleton
    fun provideGroupDao(appDatabase: AppDatabase): GroupDao {
        return appDatabase.groupDao()
    }

    @Provides
    @Singleton
    fun provideTrafficRecordDao(appDatabase: AppDatabase): TrafficRecordDao {
        return appDatabase.trafficRecordDao()
    }
}
