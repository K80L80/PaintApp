package com.example.paintapp
import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "drawing")
data class Drawing(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @Ignore val bitmap: Bitmap // Active bitmap used for drawing (ignored by Room) since it’s not stored directly in the database
    val actualPathToBitmap: String, // File path to the bitmap (stored externally)
    val userFileName: String // Front-end name
)

//@Entity(tableName = "Drawings")
//data class DrawEntity(
//    var name: String,
//    var bitmap: String
//) {
//    @PrimaryKey(autoGenerate = true)
//    var id: Int = 0
//}
