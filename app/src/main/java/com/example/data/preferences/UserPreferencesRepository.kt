package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

enum class AppThemeMode {
    SYSTEM, DARK, LIGHT
}

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val HAPTIC_ENABLED = booleanPreferencesKey("haptic_enabled")
    }

    val themeMode: Flow<AppThemeMode> = context.dataStore.data
        .map { preferences ->
            val modeName = preferences[PreferencesKeys.THEME_MODE] ?: AppThemeMode.SYSTEM.name
            try {
                AppThemeMode.valueOf(modeName)
            } catch (e: Exception) {
                AppThemeMode.SYSTEM
            }
        }

    val hapticEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.HAPTIC_ENABLED] ?: true
        }

    suspend fun setThemeMode(mode: AppThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode.name
        }
    }

    suspend fun setHapticEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.HAPTIC_ENABLED] = enabled
        }
    }
}
