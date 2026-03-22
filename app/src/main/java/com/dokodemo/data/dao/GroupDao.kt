package com.dokodemo.data.dao

import androidx.room.*
import com.dokodemo.data.model.Group
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupDao {

    /** 监听所有分组列表（按 order 排序，Flow 实时更新） */
    @Query("SELECT * FROM groups ORDER BY `order` ASC, createdAt ASC")
    fun getAllGroups(): Flow<List<Group>>

    /** 获取单个分组 */
    @Query("SELECT * FROM groups WHERE id = :id")
    suspend fun getGroupById(id: Long): Group?

    /** 新增分组 */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: Group): Long

    /** 删除分组（节点不会被删除，groupId 会变成 null） */
    @Delete
    suspend fun deleteGroup(group: Group)

    /** 更新分组 */
    @Update
    suspend fun updateGroup(group: Group)

    /** 删除分组后，将该分组下所有节点的 groupId 置为 null */
    @Query("UPDATE server_profiles SET groupId = NULL WHERE groupId = :groupId")
    suspend fun unassignNodesFromGroup(groupId: Long)

    /** 通过订阅 ID 找到对应分组 */
    @Query("SELECT * FROM groups WHERE subscriptionId = :subscriptionId LIMIT 1")
    suspend fun getGroupBySubscriptionId(subscriptionId: Long): Group?
}
