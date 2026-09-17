package com.dokodemo.core

import androidx.room.withTransaction
import com.dokodemo.data.AppDatabase
import com.dokodemo.data.model.Group
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubscriptionSyncManager @Inject constructor(private val db: AppDatabase, private val fetcher: SubscriptionFetcher) {
    private val mutex = Mutex()
    suspend fun refresh(id: Long): Result<Int> = mutex.withLock {
        try {
            val before = requireNotNull(db.subscriptionDao().getSubscriptionById(id))
            val (nodes, info) = fetcher.fetchAndParse(before.url, null).getOrThrow()
            db.withTransaction {
                val current = requireNotNull(db.subscriptionDao().getSubscriptionById(id))
                check(current.url == before.url) { "Subscription changed; refresh again" }
                val groupId = db.groupDao().getGroupBySubscriptionId(id)?.id
                    ?: db.groupDao().insertGroup(Group(name = current.name, subscriptionId = id))
                db.serverDao().replaceSubscription(id, nodes.map { it.copy(groupId = groupId) })
                db.subscriptionDao().updateSyncStatus(id, System.currentTimeMillis(), nodes.size,
                    info?.upload ?: current.upload, info?.download ?: current.download,
                    info?.total ?: current.total, info?.expire ?: current.expire)
            }
            Result.success(nodes.size)
        } catch (e: CancellationException) { throw e } catch (e: Exception) { Result.failure(e) }
    }
}
