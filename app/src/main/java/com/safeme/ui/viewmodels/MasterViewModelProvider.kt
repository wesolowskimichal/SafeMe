package com.safeme.ui.viewmodels

import android.app.Application
import android.media.MediaPlayer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.safeme.db.SettingsDataStore
import com.safeme.repository.ContactsRepository

class MasterViewModelProvider(
    val dataStore: SettingsDataStore,
    val mediaPlayer: MediaPlayer
): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MasterViewModel(dataStore, mediaPlayer) as T
    }
}