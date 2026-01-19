package com.dokodemo.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore
    
    // Keys
    private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
    private val PROXIED_APPS_KEY = stringSetPreferencesKey("proxied_apps")
    
    // Getters
    val isDarkMode: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[DARK_MODE_KEY] ?: true // Default to Dark Mode
        }
        
    val proxiedApps: Flow<Set<String>> = dataStore.data
        .map { preferences ->
            preferences[PROXIED_APPS_KEY] ?: emptySet()
        }
        
    // Setters
    suspend fun setDarkMode(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[DARK_MODE_KEY] = enabled
        }
    }
    
    suspend fun setProxiedApps(packages: Set<String>) {
        dataStore.edit { preferences ->
            preferences[PROXIED_APPS_KEY] = packages
        }
    }
    
    suspend fun addProxiedApp(packageName: String) {
        dataStore.edit { preferences ->
            val current = preferences[PROXIED_APPS_KEY] ?: emptySet()
            preferences[PROXIED_APPS_KEY] = current + packageName
        }
    }
    
    suspend fun removeProxiedApp(packageName: String) {
        dataStore.edit { preferences ->
            val current = preferences[PROXIED_APPS_KEY] ?: emptySet()
            preferences[PROXIED_APPS_KEY] = current - packageName
        }
    }
}
