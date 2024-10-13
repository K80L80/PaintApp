package com.example.paintapp

import android.graphics.Bitmap

data class Drawing(
    val id: Long,
    val bitmap: Bitmap,   // In-memory bitmap used by the app
    val fileName: String,  // Filename, full path
    val userChosenFileName: String //User chosen file name, just for display sake
)

