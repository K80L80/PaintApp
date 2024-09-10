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
import androidx.lifecycle.LifecycleOwner

class CustomDrawView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private var bitmap: Bitmap? = null
    private var bitmapCanvas: Canvas? = null
    private val path = Path()
    private var paintTool: PaintTool = PaintTool()

    // Coordinates used for drawing shapes
    private var startX = 0f
    private var startY = 0f
    private var endX = 0f
    private var endY = 0f
    fun setUpViewModelObservers(drawViewModel: DrawViewModel) {
        // Observe changes to the PaintTool object
        drawViewModel.paintTool.observe(context as LifecycleOwner) { newPaintTool ->
            paintTool = newPaintTool
            invalidate() // Redraw the view with the updated tool
        }

        // Observe changes to the bitmap
        drawViewModel.bitmap.observe(context as LifecycleOwner) { newBitmap ->
            if (newBitmap != null) {
                bitmap = newBitmap
                bitmapCanvas = Canvas(newBitmap)
                invalidate()
            }
            // If no bitmap exists, create a default bitmap
            else if(bitmap == null) {
                bitmap = Bitmap.createBitmap(800, 800, Bitmap.Config.ARGB_8888)
                bitmapCanvas = Canvas(bitmap!!)
                bitmapCanvas?.drawColor(Color.WHITE)
                drawViewModel.setBitmap(bitmap) //add bitmap to view model
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(bitmap!!, 0f, 0f, null)
        when (paintTool.shape) {
            "free" -> {
                paintTool.paint.style = Paint.Style.STROKE
                canvas.drawPath(path, paintTool.paint)
            }
            "line" -> {
                paintTool.paint.style = Paint.Style.STROKE
                canvas.drawLine(startX, startY, endX, endY, paintTool.paint)
            }
            "circle" -> {
                val radius = Math.sqrt(
                    Math.pow((endX - startX).toDouble(), 2.0) + Math.pow((endY - startY).toDouble(), 2.0)
                ).toFloat()
                paintTool.paint.style = Paint.Style.FILL
                canvas.drawCircle(startX, startY, radius, paintTool.paint)
            }
            "square" -> {
                val side = Math.min(Math.abs(endX - startX), Math.abs(endY - startY))
                paintTool.paint.style = Paint.Style.FILL
                canvas.drawRect(startX, startY, startX + side, startY + side, paintTool.paint)
            }
            "rectangle" -> {
                paintTool.paint.style = Paint.Style.FILL
                canvas.drawRect(startX, startY, endX, endY, paintTool.paint)
            }
            "diamond" -> {
                val diamondPath = Path().apply {
                    moveTo((startX + endX) / 2, startY)
                    lineTo(endX, (startY + endY) / 2)
                    lineTo((startX + endX) / 2, endY)
                    lineTo(startX, (startY + endY) / 2)
                    close()
                }
                paintTool.paint.style = Paint.Style.FILL
                canvas.drawPath(diamondPath, paintTool.paint)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = event.x
                startY = event.y
                endX = startX
                endY = startY
                if (paintTool.shape == "free") {
                    path.reset()
                    path.moveTo(startX, startY)
                }
                invalidate()
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                endX = event.x
                endY = event.y
                if (paintTool.shape == "free") {
                    path.lineTo(endX, endY)
                    bitmapCanvas?.drawPath(path, paintTool.paint)
                }
                invalidate()
                return true
            }
            MotionEvent.ACTION_UP -> {
                endX = event.x
                endY = event.y
                when (paintTool.shape) {
                    "line" -> bitmapCanvas?.drawLine(startX, startY, endX, endY, paintTool.paint)
                    "circle" -> {
                        val radius = Math.sqrt(
                            Math.pow((endX - startX).toDouble(), 2.0) + Math.pow((endY - startY).toDouble(), 2.0)
                        ).toFloat()
                        bitmapCanvas?.drawCircle(startX, startY, radius, paintTool.paint)
                    }
                    "square" -> {
                        val side = Math.min(Math.abs(endX - startX), Math.abs(endY - startY))
                        bitmapCanvas?.drawRect(startX, startY, startX + side, startY + side, paintTool.paint)
                    }
                    "rectangle" -> {
                        bitmapCanvas?.drawRect(startX, startY, endX, endY, paintTool.paint)
                    }
                    "diamond" -> {
                        val diamondPath = Path().apply {
                            moveTo((startX + endX) / 2, startY)
                            lineTo(endX, (startY + endY) / 2)
                            lineTo((startX + endX) / 2, endY)
                            lineTo(startX, (startY + endY) / 2)
                            close()
                        }
                        bitmapCanvas?.drawPath(diamondPath, paintTool.paint)
                    }
                    "free" -> {
                        bitmapCanvas?.drawPath(path, paintTool.paint)
                        path.reset()
                    }
                }
                invalidate()
                return true
            }
            else -> return false
        }
    }




    fun setBitmap(bitmap: Bitmap) {
        this.bitmap = bitmap
        this.bitmapCanvas = Canvas(bitmap)
        invalidate()
    }

    fun resetDrawing() {
        path.reset()  // Clear the path for freehand drawing
        bitmap?.eraseColor(Color.WHITE)  // Clear the bitmap by filling it with white
        bitmapCanvas?.drawColor(Color.WHITE)  // Clear the canvas by filling it with white
        invalidate()  // Redraw the view to reflect the reset
    }

    fun getBitmap(): Bitmap? {
        return bitmap
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
