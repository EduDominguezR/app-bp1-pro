package com.example.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "baseus_pulse_prefs")

enum class AppThemeMode {
    DARK,
    LIGHT,
    LIQUID_GLASS;

    companion object {
        fun fromString(value: String?): AppThemeMode {
            return when (value?.uppercase()) {
                "LIGHT" -> LIGHT
                "LIQUID_GLASS" -> LIQUID_GLASS
                else -> DARK
            }
        }
    }
}

class PreferencesManager(private val context: Context) {

    companion object {
        val KEY_ACTIVE_PRESET = stringPreferencesKey("active_preset")
        val KEY_BAND_GAINS = stringPreferencesKey("band_gains")
        val KEY_PREAMP_DB = floatPreferencesKey("preamp_db")
        val KEY_LIMITER_ENABLED = booleanPreferencesKey("limiter_enabled")
        val KEY_POPUP_ENABLED = booleanPreferencesKey("popup_enabled")
        val KEY_LAST_MAC = stringPreferencesKey("last_mac")
        val KEY_LAST_ANC_MODE = intPreferencesKey("last_anc_mode")
        val KEY_LAST_GAME_MODE = booleanPreferencesKey("last_game_mode")
        val KEY_FORCE_DARK_THEME = booleanPreferencesKey("force_dark_theme")
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
    }

    val activePresetName: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_ACTIVE_PRESET] ?: "ORIGINAL"
    }

    val bandGains: Flow<List<Float>> = context.dataStore.data.map { prefs ->
        val raw = prefs[KEY_BAND_GAINS] ?: "0,0,0,0,0,0,0,0,0,0"
        raw.split(",").mapNotNull { it.toFloatOrNull() }.ifEmpty { List(10) { 0f } }
    }

    val preampDb: Flow<Float> = context.dataStore.data.map { prefs ->
        prefs[KEY_PREAMP_DB] ?: 0f
    }

    val isLimiterEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_LIMITER_ENABLED] ?: true
    }

    val isPopupEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_POPUP_ENABLED] ?: true
    }

    val lastMacAddress: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_LAST_MAC]
    }

    val lastAncMode: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[KEY_LAST_ANC_MODE] ?: 0
    }

    val lastGameMode: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_LAST_GAME_MODE] ?: false
    }

    val themeMode: Flow<AppThemeMode> = context.dataStore.data.map { prefs ->
        val raw = prefs[KEY_THEME_MODE]
        if (raw != null) {
            AppThemeMode.fromString(raw)
        } else {
            // Backward-compatible migration from boolean
            val isDark = prefs[KEY_FORCE_DARK_THEME] ?: true
            if (isDark) AppThemeMode.DARK else AppThemeMode.LIGHT
        }
    }

    val isDarkTheme: Flow<Boolean> = themeMode.map { mode ->
        mode != AppThemeMode.LIGHT
    }

    suspend fun savePreset(name: String, gains: List<Float>) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ACTIVE_PRESET] = name
            prefs[KEY_BAND_GAINS] = gains.joinToString(",")
        }
    }

    suspend fun savePreamp(db: Float) {
        context.dataStore.edit { prefs ->
            prefs[KEY_PREAMP_DB] = db
        }
    }

    suspend fun saveLimiterEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_LIMITER_ENABLED] = enabled
        }
    }

    suspend fun savePopupEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_POPUP_ENABLED] = enabled
        }
    }

    suspend fun saveLastMac(mac: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_LAST_MAC] = mac
        }
    }

    suspend fun saveAncMode(mode: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_LAST_ANC_MODE] = mode
        }
    }

    suspend fun saveGameMode(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_LAST_GAME_MODE] = enabled
        }
    }

    suspend fun saveThemeMode(mode: AppThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[KEY_THEME_MODE] = mode.name
            prefs[KEY_FORCE_DARK_THEME] = (mode != AppThemeMode.LIGHT)
        }
    }

    suspend fun saveDarkTheme(isDark: Boolean) {
        saveThemeMode(if (isDark) AppThemeMode.DARK else AppThemeMode.LIGHT)
    }
}
