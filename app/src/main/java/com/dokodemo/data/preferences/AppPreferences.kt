package com.dokodemo.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
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

enum class SplitTunnelingMode(val displayName: String) {
    PROXY_SELECTED("仅选中应用走代理"),
    BYPASS_SELECTED("仅选中应用直连")
}

enum class CustomRuleMatchType(val displayName: String) {
    DOMAIN_FULL("完整域名"),
    DOMAIN_SUFFIX("域名后缀"),
    DOMAIN_KEYWORD("域名关键词"),
    IP_CIDR("IP / CIDR"),
    GEOSITE("GeoSite"),
    GEOIP("GeoIP")
}

enum class CustomRuleAction(val displayName: String) {
    PROXY("走代理"),
    DIRECT("直连"),
    BLOCK("拦截")
}

data class CustomRoutingRule(
    val id: String,
    val name: String,
    val matchType: CustomRuleMatchType,
    val value: String,
    val action: CustomRuleAction,
    val enabled: Boolean = true
)

@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore
    private val gson = Gson()
    private val customRuleListType = object : TypeToken<List<CustomRoutingRule>>() {}.type

    private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
    private val FONT_SIZE_KEY = floatPreferencesKey("font_size")
    private val PROXIED_APPS_KEY = stringSetPreferencesKey("proxied_apps")
    private val ROUTING_MODE_KEY = stringPreferencesKey("routing_mode")
    private val MUX_ENABLED_KEY = booleanPreferencesKey("mux_enabled")
    private val ALLOW_INSECURE_KEY = booleanPreferencesKey("allow_insecure")
    private val UDP_ENABLED_KEY = booleanPreferencesKey("udp_enabled")
    private val SPLIT_TUNNELING_MODE_KEY = stringPreferencesKey("split_tunneling_mode")
    private val CUSTOM_ROUTING_RULES_KEY = stringPreferencesKey("custom_routing_rules")

    val isDarkMode: Flow<Boolean> = dataStore.data
        .map { it[DARK_MODE_KEY] ?: false }

    val fontSizeScale: Flow<Float> = dataStore.data
        .map { it[FONT_SIZE_KEY] ?: 1.0f }

    val proxiedApps: Flow<Set<String>> = dataStore.data
        .map { it[PROXIED_APPS_KEY] ?: emptySet() }

    val routingMode: Flow<RoutingMode> = dataStore.data
        .map { prefs ->
            val saved = prefs[ROUTING_MODE_KEY] ?: RoutingMode.GLOBAL.name
            runCatching { RoutingMode.valueOf(saved) }.getOrDefault(RoutingMode.GLOBAL)
        }

    val muxEnabled: Flow<Boolean> = dataStore.data
        .map { it[MUX_ENABLED_KEY] ?: false }

    val allowInsecure: Flow<Boolean> = dataStore.data
        .map { it[ALLOW_INSECURE_KEY] ?: false }

    val udpEnabled: Flow<Boolean> = dataStore.data
        .map { it[UDP_ENABLED_KEY] ?: true }

    val splitTunnelingMode: Flow<SplitTunnelingMode> = dataStore.data
        .map { prefs ->
            val saved = prefs[SPLIT_TUNNELING_MODE_KEY] ?: SplitTunnelingMode.PROXY_SELECTED.name
            runCatching { SplitTunnelingMode.valueOf(saved) }.getOrDefault(SplitTunnelingMode.PROXY_SELECTED)
        }

    val customRoutingRules: Flow<List<CustomRoutingRule>> = dataStore.data
        .map { prefs ->
            val saved = prefs[CUSTOM_ROUTING_RULES_KEY].orEmpty()
            if (saved.isBlank()) {
                emptyList()
            } else {
                runCatching {
                    gson.fromJson<List<CustomRoutingRule>>(saved, customRuleListType)
                }.getOrDefault(emptyList())
            }
        }

    suspend fun setDarkMode(enabled: Boolean) {
        dataStore.edit { it[DARK_MODE_KEY] = enabled }
    }

    suspend fun setFontSizeScale(scale: Float) {
        dataStore.edit { it[FONT_SIZE_KEY] = scale }
    }

    suspend fun setRoutingMode(mode: RoutingMode) {
        dataStore.edit { it[ROUTING_MODE_KEY] = mode.name }
    }

    suspend fun setMuxEnabled(enabled: Boolean) {
        dataStore.edit { it[MUX_ENABLED_KEY] = enabled }
    }

    suspend fun setAllowInsecure(enabled: Boolean) {
        dataStore.edit { it[ALLOW_INSECURE_KEY] = enabled }
    }

    suspend fun setUdpEnabled(enabled: Boolean) {
        dataStore.edit { it[UDP_ENABLED_KEY] = enabled }
    }

    suspend fun setSplitTunnelingMode(mode: SplitTunnelingMode) {
        dataStore.edit { it[SPLIT_TUNNELING_MODE_KEY] = mode.name }
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

    suspend fun setCustomRoutingRules(rules: List<CustomRoutingRule>) {
        dataStore.edit { it[CUSTOM_ROUTING_RULES_KEY] = gson.toJson(rules) }
    }
}
