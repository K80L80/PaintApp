package com.example.paintapp

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Entity(tableName = "drawings")
@Serializable
data class Drawing(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,

    @Ignore
    @Transient
    val bitmap: Bitmap? = null,  // Serialization ignores this field, since it will be sent as file

    val fileName: String,  // Full path of the file

    var imageTitle: String, // User-chosen name for display purposes

    var ownerID: Long //the user who owns this drawing
)
