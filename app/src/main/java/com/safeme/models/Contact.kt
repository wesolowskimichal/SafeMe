package com.safeme.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(
    tableName = "contacts"
)
@Serializable
data class Contact (
    @PrimaryKey(autoGenerate = true)
    val id: Long? = null,
    val name: String,
    val number: String,
    var fav: Boolean = false
)