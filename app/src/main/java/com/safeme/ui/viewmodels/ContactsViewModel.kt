package com.safeme.ui.viewmodels

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.database.Cursor
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safeme.models.Contact
import com.safeme.repository.ContactsRepository
import kotlinx.coroutines.launch
import android.provider.ContactsContract
import android.util.Log
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.safeme.db.SettingsDataStore
import com.safeme.models.Constants
import com.safeme.models.ExtContact
import com.safeme.models.Timer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext


class ContactsViewModel(
    val application: Application,
    val contactsRepository: ContactsRepository,
    val dataStore: SettingsDataStore
): ViewModel() {
    private var _contacts: MutableLiveData<List<Contact>> = MutableLiveData()
    val phoneContacts: LiveData<List<Contact>>
        get() = _contacts



    // --- timer ---
    private var _minutes: MutableLiveData<Int> = MutableLiveData(0)
    val minutes: LiveData<Int>
        get() = _minutes
    private var _seconds: MutableLiveData<Int> = MutableLiveData(0)
    val seconds: LiveData<Int>
        get() = _seconds
    fun addMinutes(minutes: Int) {
        _minutes.postValue(addTime(minutes, _minutes.value ?: 0))
    }
    fun addSeconds(seconds: Int) {
        _seconds.postValue(addTime(seconds, _seconds.value ?: 0))
    }

    private fun addTime(addedTime: Int, currentTime: Int): Int {
        val sum = currentTime + addedTime
        return when {
            sum >= 60 -> 59
            sum < 0 -> 0
            else -> sum
        }
    }
    // --- ----- ---


    init {
        viewModelScope.launch {
            dataStore.readTimer.collect{time ->
                val (minutes, seconds) = time.split(":")
                _minutes.postValue(minutes.toInt())
                _seconds.postValue(seconds.toInt())
            }
        }
    }

    suspend fun saveContact(contact: Contact): Long{
        return contactsRepository.upsert(contact)
    }

    suspend fun updateFavByPhoneNumber(phoneNumber: String, isFavorite: Boolean) = withContext(Dispatchers.IO) {
        contactsRepository.updateFavByPhoneNumber(phoneNumber, isFavorite)
    }
    suspend fun clearAllContacts() = viewModelScope.launch {
        contactsRepository.clearAllContacts()
    }


    suspend fun saveContacts(contacts: List<Contact>) {
        for (contact in contacts) {
            saveContact(contact)
        }
    }

    suspend fun getContactByPhoneNumber(phoneNumber: String): Contact? {
        return withContext(Dispatchers.IO) {
            contactsRepository.getContactByPhoneNumber(phoneNumber)
        }
    }

    fun getAllContacts(savedContacts: ArrayList<Contact>): LiveData<List<Contact>> {
        viewModelScope.launch {
            _contacts.postValue(getContactsList(savedContacts))
        }
        return phoneContacts
    }

    private fun getContactsList(savedContacts: ArrayList<Contact>): List<Contact> {
        val contacts = mutableListOf<Contact>()
        val contentResolver = application.applicationContext.contentResolver
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )

        val cursor: Cursor? = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection,
            null,
            null,
            null
        )

        cursor?.use {
            while (it.moveToNext()) {
                val nameIndex =
                    it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numberIndex =
                    it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

                val name = it.getString(nameIndex)
                val number = it.getString(numberIndex)

                val isFavorited = savedContacts.any { savedContact ->
                    savedContact.number == number
                }

                val contact = Contact(name = name, number = number, fav = isFavorited)
                contacts.add(contact)
            }
        }

        cursor?.close()

        return contacts
    }

    fun getSavedContacts() = contactsRepository.getSavedContacts()

    fun deleteContact(contact: Contact) = viewModelScope.launch {
        contactsRepository.deleteContact(contact)
    }
}