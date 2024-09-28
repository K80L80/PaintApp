package com.example.paintapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Drawings")
data class DrawEntity(
    var name: String,
    var bitmap: String
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}