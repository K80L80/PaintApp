/**Model for all of the functionality and member variables of draw
 * Date: 09/08/2024
 *
 */
package com.example.paintapp

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.view.MotionEvent
import androidx.lifecycle.ViewModel
import android.graphics.Color
import android.util.Log

class DrawViewModel : ViewModel() {
    private val paint = Paint()
    private val path = Path()
    private var bitmap: Bitmap? = Bitmap.createBitmap(800, 800, Bitmap.Config.ARGB_8888)
    private var bitmapCanvas: Canvas? = Canvas(bitmap!!)

    private var currentColor = Color.BLACK
    private var currentSize = 5f
    private var currentShape = "free"
    private var currentStyle = Paint.Style.STROKE

    private var startX = 0f
    private var startY = 0f
    private var endX = 0f
    private var endY = 0f

    /**Handles initialization for our bitmap and paint objects.
     *
     */
    init {
        paint.color = currentColor
        paint.style = currentStyle
        paint.strokeWidth = currentSize
        paint.isAntiAlias = true
        paint.strokeCap = Paint.Cap.ROUND
        //set our background to white on startup.
        bitmap?.let { bitmapCanvas?.drawColor(Color.WHITE) }
    }

    /**Method that handles drawing on the screen including shape generation.
     *
     */
    fun onDraw(canvas: Canvas) {
        bitmap?.let { bmp ->
            canvas.drawBitmap(bmp, 0f, 0f, null)
        }
        paint.style = if (currentShape == "free" || currentShape == "line") Paint.Style.STROKE else Paint.Style.FILL

        when (currentShape) {
            "line", "free" -> bitmapCanvas?.drawPath(path, paint)
            "circle" -> {
                val radius = Math.sqrt(
                    Math.pow((endX - startX).toDouble(), 2.0) + Math.pow((endY - startY).toDouble(), 2.0)
                ).toFloat()
                bitmapCanvas?.drawCircle(startX, startY, radius, paint)
            }
            "square" -> {
                val side = Math.min(Math.abs(endX - startX), Math.abs(endY - startY))
                bitmapCanvas?.drawRect(startX, startY, startX + side, startY + side, paint)
            }
            "rectangle" -> bitmapCanvas?.drawRect(startX, startY, endX, endY, paint)
            "diamond" -> {
                val diamondPath = Path().apply {
                    moveTo((startX + endX) / 2, startY)
                    lineTo(endX, (startY + endY) / 2)
                    lineTo((startX + endX) / 2, endY)
                    lineTo(startX, (startY + endY) / 2)
                    close()
                }
                bitmapCanvas?.drawPath(diamondPath, paint)
            }
        }
    }

    /**Method to handle on touch events for when the user clicks on the screen.
     *
     */
    fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (currentShape == "free" || currentShape == "line") {
                    path.moveTo(event.x, event.y)
                } else {
                    startX = event.x
                    startY = event.y
                    endX = event.x
                    endY = event.y
                }
                return true
            }
            //action move doesn't handle up and down, results in stuttering, weird starting position vals.
            MotionEvent.ACTION_MOVE -> {
                if (currentShape == "free" || currentShape == "line") {
                    path.lineTo(event.x, event.y)
                } else {
                    endX = event.x
                    endY = event.y
                }
                return true
            }
            MotionEvent.ACTION_UP -> {
                if (currentShape == "free" || currentShape == "line") {
                    path.lineTo(event.x, event.y)
                    bitmapCanvas?.drawPath(path, paint)
                    path.reset()
                } else {
                    endX = event.x
                    endY = event.y
                    bitmapCanvas?.drawPath(path, paint)
                    path.reset()
                }
                return true
            }
            else -> return false
        }
    }

    /**Method to set the color of our paint object
     *
     */
    fun setColor(color: Int) {
        paint.color = color
    }

    /**Method to set the size of our paint object
     *
     */
    fun setSize(size: Float) {
        paint.strokeWidth = size
    }

    /**Method to set the shape of our paint object
     *
     */
    fun setShape(shape: String) {
        currentShape = shape
        if (shape == "free") {
            paint.style = Paint.Style.STROKE
            path.reset()
        } else {
            paint.style = Paint.Style.FILL
        }
    }

    /**Method to set the screen to white to start over.
     *
     */
    fun resetDrawing() {
        path.reset()
        bitmapCanvas?.drawColor(Color.WHITE)
    }

    /**Returns the current bitmap of the model
     *
     */
    fun getBitmap(): Bitmap? {
        return bitmap
    }

    /**Sets the current bitmap of the model
     *
     */
    fun setBitmap(bitmap: Bitmap) {
        this.bitmap = bitmap
        this.bitmapCanvas = Canvas(bitmap)
    }

    /**Method to handle screen rotation, and resetting the bitmap on rotation.
     *
     */
    fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        Log.d("CustomView", "onSizeChanged called with w: $w, h: $h, oldw: $oldw, oldh: $oldh")
        //No user drawing yet, need a new bitmap to store that data
        if (bitmap == null) {
            Log.d("CustomView", "new bitmap created")
            // Create a new Bitmap if none exists
            bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            bitmapCanvas = Canvas(bitmap!!)
        }
        //rotation of screen occurred rescale drawing
        else if (w != oldw || h != oldh){
            Log.d("CustomView", "new bitmap created")
            // Resize the existing Bitmap to fit the new dimensions
            val scaledBitmap = Bitmap.createScaledBitmap(bitmap!!, w, h, true)
            bitmap = scaledBitmap
            bitmapCanvas = Canvas(scaledBitmap)
            bitmapCanvas = Canvas(bitmap!!)
        }
    }


}
