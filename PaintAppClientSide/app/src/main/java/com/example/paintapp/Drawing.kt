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
    var ownerID: String, //the user who owns this drawing
//    var downloadUrl: String? = null, // Server download URL (nullable for local-only files)

    @Ignore //bitmap is ignored by room database (since its saved to file on disk)
    @Transient //bitmap is ignored during Serialization process
    val bitmap: Bitmap? = null,  //bitmap used in-memory operations such as modifying bitmap on user interactions

) {
    // Secondary constructor for Room
//    constructor(id: Long, fileName: String, imageTitle: String, ownerID: String, downloadUrl: String?) :
//            this(id, fileName, imageTitle, ownerID, downloadUrl ,null)

    constructor(id: Long, fileName: String, imageTitle: String, ownerID: String) :
            this(id, fileName, imageTitle, ownerID, null)
}
