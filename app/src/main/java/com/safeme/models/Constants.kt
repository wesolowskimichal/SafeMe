package com.safeme.models

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object Constants {
    val TIMER = stringPreferencesKey("TIMER")
    val STOP = booleanPreferencesKey("STOP")
}