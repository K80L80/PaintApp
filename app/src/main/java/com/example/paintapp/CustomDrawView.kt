/**Custom view that handles drawing, touch events, and rotation.
 * Date:09/12/2024
 *
 */
package com.example.paintapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Path
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View

class CustomDrawView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private var bitmap: Bitmap? = null
    private var userCanvas: Canvas? = null //while the user canvas in the custom view handles touch detection and rendering on the screen.
    private val path = Path()
    private var paintTool: PaintTool = PaintTool()
    private var isUserDrawing = false

    // Member variable coordinates stored for drawing
    private var startX = 0f
    private var startY = 0f
    private var endX = 0f
    private var endY = 0f

    // Lambda member variable for drawing action (will be set by the fragment and the logic of actually drawing delegated to the view model)
    var onShapeDrawAction: ((Float, Float, MotionEvent) -> Unit)? = null

    // Callback to notify the fragment during a screen rotation
    var onSizeChangedCallback: ((Int, Int) -> Unit)? = null

    /**This sets up the observers used by the fragment to monitor changes
     * to the bitmap and paint tool.
     *
     */
//    fun setUpViewModelObservers(drawViewModel: DrawViewModel) {
//        // Observe changes to the PaintTool object
//        drawViewModel.paintTool.observe(context as LifecycleOwner) { newPaintTool ->
//            paintTool = newPaintTool
//            //invalidate notifies
//            invalidate()
//        }
//
//        drawViewModel.bitmap.observe(context as LifecycleOwner) { newBitmap ->
//            if (newBitmap != null) {
//                bitmap = newBitmap
//                bitmapCanvas = Canvas(newBitmap)
//                invalidate()
//            }
//            // If no bitmap exists, create a default bitmap
//            else if(bitmap == null) {
//                bitmap = Bitmap.createBitmap(800, 800, Bitmap.Config.ARGB_8888)
//                bitmapCanvas = Canvas(bitmap!!)
//                bitmapCanvas?.drawColor(Color.WHITE)
//                //pass bitmap to model
//                drawViewModel.setBitmap(bitmap)
//            }
//        }
//    }

    /**Handles user touch events to store positions for drawing shapes.
     * also includes a boolean to ensure onDraw is only called when the user is drawing.
     */
    //1) User touches custom view and android triggers the 'onTouchEvent' method supplying us with the event which captured the x and y coordinates and the event itself
    override fun onTouchEvent(event: MotionEvent): Boolean {
         //2) onShapeDrawAction property (lambda variable) is invoked.This lambda was assigned in the fragment,  it actually calls the logic defined in the fragment
        Log.i("CustomDrawView", "1 - touch detected, responds by calling the logic the fragment previously set relay x and y")
        onShapeDrawAction?.invoke(event.x, event.y, event)  // Callback to Fragment
        return true
    }
//        when (event.action) {
//            MotionEvent.ACTION_DOWN -> {
//                //this tells the other events like onDraw when a user starts actually drawing.
//                isUserDrawing = true
//
//                startX = event.x
//                startY = event.y
//                endX = startX
//                endY = startY
//
//                if (paintTool.shape == "free") {
//                    path.reset()
//                    path.moveTo(startX, startY)
//                }
//                //updates as the user moves their mouse
//                invalidate()
//                return true
//            }
//
//            MotionEvent.ACTION_MOVE -> {
//                endX = event.x
//                endY = event.y
//
//                if (paintTool.shape == "free") {
//                    path.lineTo(endX, endY)
//                }
//                //updates as the user moves their mouse
//                invalidate()
//                return true
//            }
//
//            MotionEvent.ACTION_UP -> {
//                endX = event.x
//                endY = event.y
//
//                //draws the shapes on the canvas
//                when (paintTool.shape) {
//                    "line" -> bitmapCanvas?.drawLine(startX, startY, endX, endY, paintTool.paint)
//                    "circle" -> {
//                        val radius = Math.sqrt(
//                            Math.pow(
//                                (endX - startX).toDouble(),
//                                2.0
//                            ) + Math.pow((endY - startY).toDouble(), 2.0)
//                        ).toFloat()
//                        bitmapCanvas?.drawCircle(startX, startY, radius, paintTool.paint)
//                    }
//
//                    "square" -> {
//                        val side = Math.min(Math.abs(endX - startX), Math.abs(endY - startY))
//                        bitmapCanvas?.drawRect(
//                            startX,
//                            startY,
//                            startX + side,
//                            startY + side,
//                            paintTool.paint
//                        )
//                    }
//
//                    "rectangle" -> bitmapCanvas?.drawRect(
//                        startX,
//                        startY,
//                        endX,
//                        endY,
//                        paintTool.paint
//                    )
//
//                    "diamond" -> {
//                        val diamondPath = Path().apply {
//                            moveTo((startX + endX) / 2, startY)
//                            lineTo(endX, (startY + endY) / 2)
//                            lineTo((startX + endX) / 2, endY)
//                            lineTo(startX, (startY + endY) / 2)
//                            close()
//                        }
//                        bitmapCanvas?.drawPath(diamondPath, paintTool.paint)
//                    }
//
//                    "free" -> {
//                        bitmapCanvas?.drawPath(path, paintTool.paint)
//                        path.reset()
//                    }
//                }
//                //update that drawing is now completed
//                isUserDrawing = false
//                //draw final shape
//                invalidate()
//                return true
//            }
//
//            else -> return false
//        }
//    }

    /**Handles drawing shapes on the canvas.
     * Uses the isDrawing boolean to ensure shapes are only drawn during touch events.
     */
//9) the onDraw method was trigger when the view realizes that it has an older verison of the bitmap and needs to display the new one (tiggered by the update bitmap method here in the view)
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Always draw the bitmap if it exists
        bitmap?.let {
            Log.i("CustomDrawView", "6b ondraw called redrawn to the userCanvas")
            canvas.drawBitmap(it, 0f, 0f, null)
        }
//
//        super.onDraw(canvas)
//        // Draw the updated bitmap onto the canvas
         // //the modifications the user made to the drawing are now reflected on the screen after this call
//        canvas.drawBitmap(currentBitmap, 0f, 0f, null)
//    }

//        // Only draw the shapes if the user is currently drawing.
//        if (isUserDrawing) {
//            when (paintTool.currentShape) {
//                "free" -> {
//                    paintTool.paint.style = Paint.Style.STROKE
//                    canvas.drawPath(path, paintTool.paint)
//                }
//                "line" -> {
//                    paintTool.paint.style = Paint.Style.STROKE
//                    canvas.drawLine(startX, startY, endX, endY, paintTool.paint)
//                }
//                "circle" -> {
//                    val radius = Math.sqrt(
//                        Math.pow((endX - startX).toDouble(), 2.0) + Math.pow((endY - startY).toDouble(), 2.0)
//                    ).toFloat()
//                    paintTool.paint.style = Paint.Style.FILL
//                    canvas.drawCircle(startX, startY, radius, paintTool.paint)
//                }
//                "square" -> {
//                    val side = Math.min(Math.abs(endX - startX), Math.abs(endY - startY))
//                    paintTool.paint.style = Paint.Style.FILL
//                    canvas.drawRect(startX, startY, startX + side, startY + side, paintTool.paint)
//                }
//                "rectangle" -> {
//                    paintTool.paint.style = Paint.Style.FILL
//                    canvas.drawRect(startX, startY, endX, endY, paintTool.paint)
//                }
//                "diamond" -> {
//                    val diamondPath = Path().apply {
//                        moveTo((startX + endX) / 2, startY)
//                        lineTo(endX, (startY + endY) / 2)
//                        lineTo((startX + endX) / 2, endY)
//                        lineTo(startX, (startY + endY) / 2)
//                        close()
//                    }
//                    paintTool.paint.style = Paint.Style.FILL
//                    canvas.drawPath(diamondPath, paintTool.paint)
//                }
//            }
//        }
    }


    /**Method used to set a bitmap and the respective canvas
     *
     */
    //8) This method was called by the fragment requesting the custom view to update its UI based on the fragment receiving word that the bitmap changed
    fun updateBitmap(bitmap: Bitmap) {
        Log.i("CustomDrawView", "6a updateBitmap called in custom view")
        this.bitmap = bitmap //8a) the bitmap (associated with the canvas for displaying the user drawing)
        this.userCanvas = Canvas(bitmap)
        invalidate() //8b Since the bitmap was updated, this method call forces the view to redraw itself by triggering the onDraw method
    }

    /**Method to reset the drawing window.
     *
     */
    fun resetDrawing() {
        // Clear the path for freehand drawing
        path.reset()
        // Clear the bitmap by filling it with white
        bitmap?.eraseColor(Color.WHITE)
        // Clear the canvas by filling it with white
        userCanvas?.drawColor(Color.WHITE)
        // Redraw the view to reflect the reset
        invalidate()
    }

    /**Method to get the bitmap from the view. Used in the fragment.
     *
     */

    fun getBitmap(): Bitmap? {
        return bitmap
    }


    /**This method is automatically called by the Android framework whenever the size of a View changes.
     * This can happen due to various reasons, such as:  Screen Rotation, Layout Changes, View Resizing
     */
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        Log.i("CustomDrawView", "2a (onSizeChange) on size change detected (screen rotation or initial configuration) invoking lambda set by fragment")
        // Notify the fragment that the size changed
        if(oldw == 0 &&  oldh == 0){
            Log.i("CustomDrawView", "2b (onSizeChange) first time screen setup newWidth=$w, newHeight=$h, oldWidth=$oldw, oldHeight=$oldh")
        }
        else{
            Log.i("CustomDrawView", "2c (onSizeChange) actual rotation newWidth=$w, newHeight=$h, oldWidth=$oldw, oldHeight=$oldh")
        }

        Log.i("CustomDrawView", "2d (onSizeChange) on SizeChangedCallback invoked")
        onSizeChangedCallback?.invoke(w, h)


//        Log.d("CustomView", "onSizeChanged called with w: $w, h: $h, oldw: $oldw, oldh: $oldh")
//        //No user drawing yet, need a new bitmap to store that data
//        if (bitmap == null) {
//            Log.d("CustomView", "new bitmap created")
//            // Create a new Bitmap if none exists
//            bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
//            bitmapCanvas = Canvas(bitmap!!)
//        }
//        //rotation of screen occurred rescale drawing
//        else if (w != oldw || h != oldh){
//            Log.d("CustomView", "new bitmap created")
//            // Resize the existing Bitmap to fit the new dimensions
//            val scaledBitmap = Bitmap.createScaledBitmap(bitmap!!, w, h, true)
//            bitmap = scaledBitmap
//            bitmapCanvas = Canvas(scaledBitmap)
//            bitmapCanvas = Canvas(bitmap!!)
//        }
//
//        invalidate()
    }
}
