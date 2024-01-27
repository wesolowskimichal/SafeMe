package com.safeme.db

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.safeme.models.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsDataStore(
    val context: Context
) {
    private val Context.dataStore by preferencesDataStore(name = "settings")
    private val dataStore = context.dataStore

    val readTimer: Flow<String>
        get() = dataStore.data.map {preferences ->
            preferences[Constants.TIMER] ?: "01:00"
        }
    val readStopOnLocChng: Flow<Boolean>
        get() = dataStore.data.map { preferences ->
            preferences[Constants.STOP] ?: true
        }

    suspend fun saveSettings(timer: String, stopOnLocChng: Boolean) {
        saveLocChange(stopOnLocChng)
        saveTimer(timer)
    }

    private suspend fun saveLocChange(stopOnLocChng: Boolean) {
        dataStore.edit { preferences->
            preferences[Constants.STOP] = stopOnLocChng
        }
    }

    private suspend fun saveTimer(timer: String) {
        dataStore.edit {preferences ->
            preferences[Constants.TIMER] = timer
        }
    }
}