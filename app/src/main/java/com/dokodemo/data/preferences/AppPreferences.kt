package com.dokodemo.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/**
 * 路由模式枚举
 *
 * GLOBAL      = 全局模式：所有 App 的流量都走代理
 *               适合需要访问 TikTok 等境外服务，最简单
 *
 * BYPASS_CN   = 绕过国内模式：中国 IP/域名直连，境外走代理
 *               国内网站速度快，境外也能访问
 *
 * SPLIT       = 分应用模式：只有在"分应用代理"里勾选的 App 走代理
 *               最精细的控制，需要手动配置
 */
enum class RoutingMode(val displayName: String) {
    GLOBAL("全局（所有流量）"),
    BYPASS_CN("绕过国内"),
    SPLIT("分应用代理")
}

@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    // ─── Key 定义 ──────────────────────────────────────────────────────────
    private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
    private val FONT_SIZE_KEY = androidx.datastore.preferences.core.floatPreferencesKey("font_size")
    private val PROXIED_APPS_KEY = stringSetPreferencesKey("proxied_apps")
    private val ROUTING_MODE_KEY = stringPreferencesKey("routing_mode")

    // ─── Flow（响应式读取） ────────────────────────────────────────────────

    /** 深色/浅色主题，默认深色 */
    val isDarkMode: Flow<Boolean> = dataStore.data
        .map { it[DARK_MODE_KEY] ?: true }

    /** 字体大小缩放比例，默认 1.0f */
    val fontSizeScale: Flow<Float> = dataStore.data
        .map { it[FONT_SIZE_KEY] ?: 1.0f }

    /** 分应用代理：已选中走代理的包名集合 */
    val proxiedApps: Flow<Set<String>> = dataStore.data
        .map { it[PROXIED_APPS_KEY] ?: emptySet() }

    /**
     * 路由模式
     * 默认 GLOBAL（全局），这样用户连上节点后所有流量（包括 TikTok）都走代理，
     * 不需要额外配置。
     */
    val routingMode: Flow<RoutingMode> = dataStore.data
        .map { prefs ->
            val saved = prefs[ROUTING_MODE_KEY] ?: RoutingMode.GLOBAL.name
            runCatching { RoutingMode.valueOf(saved) }.getOrDefault(RoutingMode.GLOBAL)
        }

    // ─── Setter（挂起函数，在协程中调用） ─────────────────────────────────

    suspend fun setDarkMode(enabled: Boolean) {
        dataStore.edit { it[DARK_MODE_KEY] = enabled }
    }

    suspend fun setFontSizeScale(scale: Float) {
        dataStore.edit { it[FONT_SIZE_KEY] = scale }
    }

    suspend fun setRoutingMode(mode: RoutingMode) {
        dataStore.edit { it[ROUTING_MODE_KEY] = mode.name }
    }

    suspend fun setProxiedApps(packages: Set<String>) {
        dataStore.edit { it[PROXIED_APPS_KEY] = packages }
    }

    suspend fun addProxiedApp(packageName: String) {
        dataStore.edit { prefs ->
            val current = prefs[PROXIED_APPS_KEY] ?: emptySet()
            prefs[PROXIED_APPS_KEY] = current + packageName
        }
    }

    suspend fun removeProxiedApp(packageName: String) {
        dataStore.edit { prefs ->
            val current = prefs[PROXIED_APPS_KEY] ?: emptySet()
            prefs[PROXIED_APPS_KEY] = current - packageName
        }
    }
}
