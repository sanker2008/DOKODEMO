package com.dokodemo.data.repository

import com.dokodemo.data.dao.SubscriptionDao
import com.dokodemo.data.model.Subscription
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubscriptionRepository @Inject constructor(
    private val subscriptionDao: SubscriptionDao
) {
    fun getAllSubscriptions(): Flow<List<Subscription>> = subscriptionDao.getAllSubscriptions()

    suspend fun getSubscriptionById(id: Long): Subscription? = subscriptionDao.getSubscriptionById(id)

    suspend fun insertSubscription(subscription: Subscription): Long = subscriptionDao.insert(subscription)

    suspend fun updateSubscription(subscription: Subscription) = subscriptionDao.update(subscription)

    suspend fun deleteSubscription(subscription: Subscription) = subscriptionDao.delete(subscription)
    
    suspend fun updateSyncStatus(id: Long, timestamp: Long, count: Int, upload: Long, download: Long, total: Long, expire: Long) {
        subscriptionDao.updateSyncStatus(id, timestamp, count, upload, download, total, expire)
    }
}
