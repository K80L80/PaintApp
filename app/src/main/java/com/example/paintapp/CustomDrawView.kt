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
import android.widget.Button

//Keep as is (View)
class CustomDrawView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private var bitmap: Bitmap? = null
    private var userCanvas: Canvas? = null //while the user canvas in the custom view handles touch detection and rendering on the screen.

    // Lambda member variable for drawing action (will be set by the fragment and the logic of actually drawing delegated to the view model)
    var onShapeDrawAction: ((Float, Float, MotionEvent) -> Unit)? = null

    // Callback to notify the fragment during a screen rotation
    var onSizeChangedCallback: ((Int, Int) -> Unit)? = null

    // Callback to notify the fragment during a screen rotation
    var onResetCallback: (() -> Unit?)? = null

    /**Handles user touch events to store positions for drawing shapes.
     * also includes a boolean to ensure onDraw is only called when the user is drawing.
     */
    override fun onTouchEvent(event: MotionEvent): Boolean { ////User touches custom view and android triggers the 'onTouchEvent' method supplying us with the event which captured the x and y coordinates and the event itself
        // onShapeDrawAction property (lambda variable) is invoked.This lambda was assigned in the fragment,  it actually calls the logic defined in the fragment
        onShapeDrawAction?.invoke(event.x, event.y, event)  // Callback to Fragment
        return true
    }

    /**Handles drawing shapes on the canvas.
     * Uses the isDrawing boolean to ensure shapes are only drawn during touch events.
     */
    //the onDraw method was trigger when the view realizes that it has an older verison of the bitmap and needs to display the new one (tiggered by the update bitmap method here in the view)
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // Always draw the bitmap if it exists
        bitmap?.let {
            canvas.drawBitmap(it, 0f, 0f, null)
        }
    }

    /**Method used to set a bitmap and the respective canvas
     *
     */
    //8) This method was called by the fragment requesting the custom view to update its UI based on the fragment receiving word that the bitmap changed
    fun updateBitmap(bitmap: Bitmap) {
        this.bitmap =  bitmap.copy(Bitmap.Config.ARGB_8888, true) //8a) the bitmap (associated with the canvas for displaying the user drawing)
        this.userCanvas = Canvas()
        invalidate() //8b Since the bitmap was updated, this method call forces the view to redraw itself by triggering the onDraw method
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
        // Notify the fragment that the size changed
        onSizeChangedCallback?.invoke(w, h)

    }
}