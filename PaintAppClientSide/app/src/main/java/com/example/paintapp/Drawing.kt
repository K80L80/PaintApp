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

    val fileName: String,  // Full path of the file

    var imageTitle: String, // User-chosen name for display purposes

    var ownerID: Long, //the user who owns this drawing

    @Ignore //bitmap is ignored by room database (since its saved to file on disk)
    @Transient //bitmap is ignored during Serialization process
    val bitmap: Bitmap? = null,  //bitmap used in-memory operations such as modifying bitmap on user interactions

){
    // Secondary constructor for Room
    constructor(id: Long, fileName: String, imageTitle: String, ownerID: Long) :
            this(id, fileName, imageTitle, ownerID, null)
}
