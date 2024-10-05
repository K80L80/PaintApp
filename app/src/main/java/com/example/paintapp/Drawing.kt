package com.example.paintapp

import android.graphics.Bitmap

data class Drawing(
    val id: Long,
    val bitmap: Bitmap,   // In-memory bitmap used by the app
    val fileName: String  // Filename for reference
)

