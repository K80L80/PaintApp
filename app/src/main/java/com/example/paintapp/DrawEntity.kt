package com.example.paintapp

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
//The database stores the file path only not the bitmap itself, that is stored on disk (in special folder private to this app called 'filesDir)'
@Entity(tableName = "drawings")
data class DrawEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fileName: String, // This stores the path or name of the file
    @Ignore val bitmap: Bitmap? = null //Bitmap ignored by Room, since that's not stored in database, but we need the field in memory still
)
