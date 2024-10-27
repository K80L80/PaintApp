package com.example.paintapp

import android.graphics.Bitmap
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class Drawing(
    val id: Long,

    @Transient
    val bitmap: Bitmap? = null,  // Serialization ignores this field, since it will be sent as file

    val fileName: String,  // Full path of the file

    var imageTitle: String // User-chosen name for display purposes
)
