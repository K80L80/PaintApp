/**Custom Draw view to handle drawing
 * Uses the draw view model
 * Date: 09/08/2024
 *
 */
package com.example.paintapp
import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class CustomDrawView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private var drawViewModel: DrawViewModel? = null

    /**Sets the draw view model variable.
     *
     */
    fun setViewModel(viewModel: DrawViewModel) {
        this.drawViewModel = viewModel
        invalidate()
    }

    /**Method to handle drawing in the view.
     * Uses the draw view models onDraw method.
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // Draw the existing bitmap
        val bitmap = drawViewModel?.getBitmap()
        if (bitmap != null) {
            canvas.drawBitmap(bitmap, 0f, 0f, null)
        }
        drawViewModel?.onDraw(canvas)
    }

    /**Method to touch events in the view
     * Uses the draw view models onTouchEvent method.
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        drawViewModel?.onTouchEvent(event)
        invalidate()
        return true
    }

    /**Method to handle rotations of the screen.
     * Uses the draw view models rotation method.
     */
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        drawViewModel?.onSizeChanged(w, h, oldw, oldh)
        invalidate()
    }
}
