package com.example.paintapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View

class CustomDrawView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private val paint = Paint()
    private val path = Path()
    private var bitmap: Bitmap? = null
    private var bitmapCanvas: Canvas? = null

    private var currentColor = Color.BLACK
    private var currentSize = 5f
    private var currentShape = "free" // default setting is Free Draw mode
    private var currentStyle = Paint.Style.STROKE // default setting is STROKE to avoid solid styles
    private var startX = 0f
    private var startY = 0f
    private var endX = 0f
    private var endY = 0f



    init {
        paint.color = currentColor
        paint.style = currentStyle
        paint.strokeWidth = currentSize
        paint.isAntiAlias = true
        paint.strokeCap = Paint.Cap.ROUND
        // Initialize the Bitmap if it doesn't exist
        if (bitmap == null) {
            bitmap = Bitmap.createBitmap(800, 800, Bitmap.Config.ARGB_8888)
            bitmapCanvas = Canvas(bitmap!!)
            bitmapCanvas?.drawColor(Color.WHITE) // Background color
        }

    }
    //process user touching input on screen by updating bitmap attached to canvas
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        Log.d("CustomView", "onDraw called")
        paint.color = currentColor
        paint.strokeWidth = currentSize


        canvas.drawBitmap(bitmap!!, 0f, 0f, paint)

        paint.style = if (currentShape == "free" || currentShape == "line") Paint.Style.STROKE else Paint.Style.FILL

        when (currentShape) {
            "line", "free" -> {  // Merge "line" and "free" logic
                paint.style = Paint.Style.STROKE
                bitmapCanvas?.drawPath(path, paint)
            }
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
            "rectangle" -> {
                bitmapCanvas?.drawRect(startX, startY, endX, endY, paint)
            }
            "diamond" -> {
                val diamondPath = Path()
                diamondPath.moveTo((startX + endX) / 2, startY)
                diamondPath.lineTo(endX, (startY + endY) / 2)
                diamondPath.lineTo((startX + endX) / 2, endY)
                diamondPath.lineTo(startX, (startY + endY) / 2)
                diamondPath.close()
                bitmapCanvas?.drawPath(diamondPath, paint)
            }
            "free" -> {
                paint.style = Paint.Style.STROKE
                bitmapCanvas?.drawPath(path, paint)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (currentShape == "free" || currentShape == "line") {
                    path.moveTo(event.x, event.y) // 设置起点
                    Log.d("CustomDrawView", "Starting Drawing at: ${event.x}, ${event.y}")
                } else {
                    startX = event.x
                    startY = event.y
                    endX = event.x
                    endY = event.y
                }
                invalidate()
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                if (currentShape == "free" || currentShape == "line") {
                    path.lineTo(event.x, event.y) // Draw, draw lines to the current position
                    Log.d("CustomDrawView", "Drawing moving to: ${event.x}, ${event.y}")
                    invalidate() // repaint
                } else {
                    endX = event.x
                    endY = event.y
                    invalidate()
                }
                return true
            }
            MotionEvent.ACTION_UP -> {
                if (currentShape == "free" || currentShape == "line") {
                    path.lineTo(event.x, event.y) // Update path at the end of painting
                    Log.d("CustomDrawView", "Ending Drawing at: ${event.x}, ${event.y}")
                } else {
                    endX = event.x
                    endY = event.y
                }
                invalidate()
                return true
            }
            else -> return false
        }
    }

    fun setColor(color: Int) {
        if (color == Color.TRANSPARENT) {
            currentColor = Color.BLACK
        } else {
            currentColor = color
        }
        invalidate()
    }

    fun setSize(size: Float) {
        currentSize = size
        paint.strokeWidth = size
        invalidate()
    }

    fun setShape(shape: String) {
        currentShape = shape
        Log.d("CustomDrawView", "Shape set to: $shape")
        if (shape == "free") {
            paint.style = Paint.Style.STROKE // make sure Free Draw is line style
            path.reset() // Clear the previous path
        } else {
            paint.style = Paint.Style.FILL // Make sure the other shapes are solid
        }
        invalidate()
    }
    fun setStyle(style: Paint.Style) {
        currentStyle = style
        invalidate()
    }

    fun resetDrawing() {
        path.reset()
        bitmapCanvas?.drawColor(Color.WHITE)
        invalidate()
    }

    fun getBitmap(): Bitmap? {
        return bitmap
    }

    fun setBitmap(bitmap: Bitmap) {
        this.bitmap = bitmap
        this.bitmapCanvas = Canvas(bitmap)
        invalidate()
    }

    //This method is automatically called by the Android framework whenever the size of a View changes.
    // This can happen due to various reasons, such as:  Screen Rotation, Layout Changes, View Resizing
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
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
        invalidate()
    }
}
