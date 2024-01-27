package com.safeme.models

import androidx.room.PrimaryKey
import java.io.Serializable

data class ExtContact (
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    val name: String,
    val number: String,
    var add: Boolean
): Serializable