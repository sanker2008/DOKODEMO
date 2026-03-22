package com.dokodemo.data.repository

import com.dokodemo.data.dao.GroupDao
import com.dokodemo.data.model.Group
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GroupRepository @Inject constructor(
    private val groupDao: GroupDao
) {
    fun getAllGroups(): Flow<List<Group>> = groupDao.getAllGroups()

    suspend fun getGroupById(id: Long): Group? = groupDao.getGroupById(id)

    suspend fun insertGroup(group: Group): Long = groupDao.insertGroup(group)

    suspend fun updateGroup(group: Group) = groupDao.updateGroup(group)

    suspend fun deleteGroup(group: Group) {
        // 先解除组内节点的绑定
        groupDao.unassignNodesFromGroup(group.id)
        groupDao.deleteGroup(group)
    }

    suspend fun getGroupBySubscriptionId(subscriptionId: Long): Group? =
        groupDao.getGroupBySubscriptionId(subscriptionId)
}
