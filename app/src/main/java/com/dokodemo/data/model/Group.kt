package com.dokodemo.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 分组实体
 *
 * 每个节点可以属于一个分组（serverProfile.groupId）。
 * 分组可以绑定一个订阅（subscriptionId），这样更新订阅时，
 * 新节点自动归入对应分组。
 *
 * 例如：
 *   分组"机场A"  →  绑定订阅 URL "https://sub.airport.com/xxx"
 *   每次刷新订阅 → 拉取新节点 → 节点归入"机场A"分组
 */
@Entity(tableName = "groups")
data class Group(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** 分组显示名称，如"默认"、"机场A" */
    val name: String,

    /**
     * 绑定的订阅 ID（可选）
     * null = 手动分组，节点需手动添加
     * 非 null = 订阅分组，刷新订阅时自动同步节点
     */
    val subscriptionId: Long? = null,

    /** 排序顺序（数字小的排前面） */
    val order: Int = 0,

    val createdAt: Long = System.currentTimeMillis()
)
