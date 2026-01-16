package com.dokodemo.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.dokodemo.data.dao.ServerDao
import com.dokodemo.data.dao.SubscriptionDao
import com.dokodemo.data.model.Converters
import com.dokodemo.data.model.ServerProfile
import com.dokodemo.data.model.Subscription

@Database(
    entities = [
        ServerProfile::class,
        Subscription::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun serverDao(): ServerDao
    abstract fun subscriptionDao(): SubscriptionDao
}
