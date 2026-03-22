package com.dokodemo.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.dokodemo.data.dao.GroupDao
import com.dokodemo.data.dao.ServerDao
import com.dokodemo.data.dao.SubscriptionDao
import com.dokodemo.data.model.Converters
import com.dokodemo.data.model.Group
import com.dokodemo.data.model.ServerProfile
import com.dokodemo.data.model.Subscription

/**
 * Room 数据库
 *
 * 版本历史：
 *   v1 → 初始版本（ServerProfile、Subscription 表）
 *   v2 → 新增 groups 表；ServerProfile 追加 kcpHeader、kcpSeed、ssMethod、groupId 列
 *
 * 注意：每次修改实体类（新增/删除字段）都必须：
 *   1. 增加 version 版本号
 *   2. 添加对应的 MIGRATION_x_y 迁移脚本
 *   3. 在 DatabaseModule 的 .addMigrations() 中注册
 * 否则 App 在已安装设备上启动会崩溃。
 */
@Database(
    entities = [
        ServerProfile::class,
        Subscription::class,
        Group::class           // v2 新增：分组表
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun serverDao(): ServerDao
    abstract fun subscriptionDao(): SubscriptionDao
    abstract fun groupDao(): GroupDao
}

/**
 * 从 v1 升级到 v2 的迁移脚本
 *
 * SQL 说明：
 * - ADD COLUMN：给已有表追加新列（不含数据的旧行会填充 DEFAULT 值）
 * - CREATE TABLE IF NOT EXISTS：创建新的 groups 表
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // 给 server_profiles 表追加 KCP 和分组相关列
        db.execSQL("ALTER TABLE server_profiles ADD COLUMN kcpHeader TEXT NOT NULL DEFAULT 'none'")
        db.execSQL("ALTER TABLE server_profiles ADD COLUMN kcpSeed TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE server_profiles ADD COLUMN ssMethod TEXT NOT NULL DEFAULT 'aes-256-gcm'")
        db.execSQL("ALTER TABLE server_profiles ADD COLUMN groupId INTEGER")

        // 创建分组表
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS groups (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                subscriptionId INTEGER,
                `order` INTEGER NOT NULL DEFAULT 0,
                createdAt INTEGER NOT NULL
            )
        """.trimIndent())

        // 创建默认分组（id=1）
        db.execSQL("""
            INSERT INTO groups (id, name, subscriptionId, `order`, createdAt)
            VALUES (1, '默认分组', NULL, 0, ${System.currentTimeMillis()})
        """.trimIndent())
    }
}
