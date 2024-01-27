package com.safeme.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.safeme.models.Contact

@Dao
interface ContactDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun upsert(contact: Contact): Long

    @Query("SELECT * FROM contacts")
    fun getAllContacts(): LiveData<List<Contact>>

    @Query("SELECT * FROM contacts WHERE fav = 1")
    fun getSavedContacts(): LiveData<List<Contact>>

    @Query("SELECT * FROM contacts WHERE number = :phoneNumber")
    fun getContactByPhoneNumber(phoneNumber: String): Contact?

    @Query("UPDATE contacts SET fav = :fav WHERE number = :phoneNumber")
    fun updateFavByPhoneNumber(phoneNumber: String, fav: Boolean)

    @Query("DELETE FROM contacts")
    suspend fun clearAllContacts()

    @Delete
    fun deleteContact(contact: Contact)
}