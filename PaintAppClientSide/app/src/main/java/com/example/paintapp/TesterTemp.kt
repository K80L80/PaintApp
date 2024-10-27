package com.example.paintapp

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

fun generateTestDrawings(): List<Drawing> {
    val drawings = mutableListOf<Drawing>()

    // Define a standard size for each bitmap
    val width = 1080
    val height = 2209

    // Create bitmaps with different shapes and filenames
    for (i in 1..5) {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        val paint = Paint().apply {
            color = Color.RED
            style = Paint.Style.FILL
        }

        when (i) {
            1 -> { // Draw a circle
                canvas.drawCircle(width / 2f, height / 2f, width / 3f, paint) // Centered circle
            }
            2 -> { // Draw a square
                canvas.drawRect(100f, 100f, 700f, 700f, paint) // Adjusted square to fit the new size
            }
            3 -> { // Draw a triangle
                val path = Path().apply {
                    moveTo(width / 2f, 100f) // Top (centered)
                    lineTo(100f, height - 100f) // Bottom-left
                    lineTo(width - 100f, height - 100f) // Bottom-right
                    close()
                }
                canvas.drawPath(path, paint)
            }
            4 -> { // Draw a line
                paint.strokeWidth = 20f
                paint.style = Paint.Style.STROKE
                canvas.drawLine(100f, 100f, width - 100f, height - 100f, paint) // Adjusted line to fit new size
            }
            5 -> { // Draw an oval
                canvas.drawOval(100f, 200f, width - 100f, height - 200f, paint) // Adjusted oval to fit new size
            }
        }

        // Generate a filename (e.g., "drawing_1.png", "drawing_2.png", etc.)
        val fileName = "drawing_$i.png"
        val id = System.currentTimeMillis() // Use current time in milliseconds as the unique id

        // Add the Drawing object to the list
        drawings.add(Drawing(id = id, bitmap = bitmap, fileName = fileName, imageTitle = "untitled" ))

        // Optional: Add delay to ensure unique IDs
        Thread.sleep(1)
    }

    return drawings
}

fun generateTestDrawingsAsLiveData(): LiveData<List<Drawing>>{
    // Directly initialize MutableLiveData with the generated test drawings
    val mutableDrawingList = MutableLiveData<List<Drawing>>()
    mutableDrawingList.value = generateTestDrawings() // Assign the test drawings to the value
    return mutableDrawingList
}