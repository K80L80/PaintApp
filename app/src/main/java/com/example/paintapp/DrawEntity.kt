package com.example.paintapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "drawings")
data class DrawEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fileName: String // This stores the path or name of the file
)
