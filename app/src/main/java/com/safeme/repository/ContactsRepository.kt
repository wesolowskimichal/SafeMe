package com.safeme.repository

import androidx.lifecycle.LiveData
import com.safeme.db.ContactDatabase
import com.safeme.models.Contact
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ContactsRepository(
    val db: ContactDatabase
) {
    suspend fun upsert(contact: Contact): Long {
        return db.getContactDao().upsert(contact)
    }

    fun getAllContacts() = db.getContactDao().getAllContacts()

    fun getSavedContacts() = db.getContactDao().getSavedContacts()

    fun getContactByPhoneNumber(phoneNumber: String): Contact? = db.getContactDao().getContactByPhoneNumber(phoneNumber)
    suspend fun clearAllContacts() = db.getContactDao().clearAllContacts()
    suspend fun updateFavByPhoneNumber(phoneNumber: String, isFavorite: Boolean) = db.getContactDao().updateFavByPhoneNumber(phoneNumber, isFavorite)

    suspend fun deleteContact(contact: Contact) = withContext(Dispatchers.IO) {
        db.getContactDao().deleteContact(contact)
    }

}