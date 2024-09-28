package com.example.paintapp

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path

data class Drawing(
    val thumbnail: Bitmap,
    val fileName: String
)

fun generateTestDrawings(): List<Drawing> {
    val drawings = mutableListOf<Drawing>()

    // Define a standard size for each bitmap
    val width = 200
    val height = 200

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
                canvas.drawCircle(width / 2f, height / 2f, width / 3f, paint)
            }
            2 -> { // Draw a square
                canvas.drawRect(50f, 50f, 150f, 150f, paint)
            }
            3 -> { // Draw a triangle
                val path = Path().apply {
                    moveTo(width / 2f, 50f) // Top
                    lineTo(50f, height - 50f) // Bottom-left
                    lineTo(width - 50f, height - 50f) // Bottom-right
                    close()
                }
                canvas.drawPath(path, paint)
            }
            4 -> { // Draw a line
                paint.strokeWidth = 10f
                paint.style = Paint.Style.STROKE
                canvas.drawLine(50f, 50f, width - 50f, height - 50f, paint)
            }
            5 -> { // Draw an oval
                canvas.drawOval(50f, 100f, width - 50f, height - 50f, paint)
            }
        }

        // Generate a filename (e.g., "drawing_1.png", "drawing_2.png", etc.)
        val fileName = "drawing_$i.png"

        // Add the Drawing object to the list
        drawings.add(Drawing(thumbnail = bitmap, fileName = fileName))
    }

    return drawings
}