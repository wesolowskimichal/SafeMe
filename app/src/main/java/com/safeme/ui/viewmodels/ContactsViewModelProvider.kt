package com.safeme.ui.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.safeme.db.SettingsDataStore
import com.safeme.repository.ContactsRepository

class ContactsViewModelProvider(
    val application: Application,
    val contactsRepository: ContactsRepository,
    val dataStore: SettingsDataStore
): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ContactsViewModel(application, contactsRepository, dataStore) as T
    }
}