/**Draw view model class for handling our live data and paint data class.
 * Date:09/12/2024
 *
 */
package com.example.paintapp

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import android.view.MotionEvent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlin.random.Random

/**Data class to store all of our paint values, such as size, shape, and color.
 *
 */
data class PaintTool(
    val paint: Paint = Paint().apply {
        color = Color.BLACK
        strokeWidth = 5f
        style = Paint.Style.STROKE
        isAntiAlias = true
    },
    val currentShape: String = "free" // Default shape is "free draw"
    //TODO: Add mode? Free?

)
//'Backend canvas' in the ViewModel treated as a tool for updating the bitmap

class DrawViewModel : ViewModel() {

    // Bitmap for storing the drawing
    private val _bitmap = MutableLiveData<Bitmap?>()
    val bitmap: LiveData<Bitmap?> get() = _bitmap

    // Backend canvas to modify the bitmap
    private var _backendCanvas: Canvas? = null

    // LiveData for the PaintTool object
    private val _paintTool = MutableLiveData<PaintTool>().apply {
        // Use postValue to ensure it's safe for background threads
        value = PaintTool()
    }
    val paintTool: LiveData<PaintTool> get() = _paintTool

    // Flag to indicate whether the drawing should be reset
    private val _shouldReset = MutableLiveData<Boolean>()

    fun getOrCreateBitmap(newWidth: Int, newHeight: Int) {
        //get the bitmap on record
        val currentBitmap = _bitmap.value

        //create new drawing for user
        if (currentBitmap == null){
            Log.i("ViewModel", "4a new bitmap was created!")
            // bitmap.value is null create a new one, make a new user bitmap to store a drawing, that matches the current height and
            val createdBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888)
            _backendCanvas = Canvas(createdBitmap)
            Log.i("ViewModel", "4b load backend canvas with new bitmap")
            _bitmap.value = createdBitmap
        }
        //restore old user drawing (the bitmap) and resize if needed
        else{
            Log.i("ViewModel", "4c restore old bitmap")
            if (currentBitmap.width != newWidth || currentBitmap.height != newHeight){
                Log.i("ViewModel", "4d bitmap needs to be scaled!")
                val scaledBitmap = Bitmap.createScaledBitmap(currentBitmap, newWidth, newHeight, true)
                _bitmap.value = scaledBitmap
                Log.i("ViewModel", "4e load backend canvas with scaled bitmap ")
                _backendCanvas = Canvas(scaledBitmap) //
            }
            else {
                Log.i("ViewModel", "4f The existing bitmap was restored, no resizing needed")
            }
        }
    }

    /**Setter for the bitmap
     *
     */
    fun setBitmap(bitmap: Bitmap?) {
        _bitmap.postValue(bitmap)
    }

    /**Method to update the color of the paint tool
     * Color comes from the user clicking on the button and then the dialog box.
     *
     */
    fun setColor(color: Int) {
        _paintTool.postValue(
            _paintTool.value?.copy(
                paint = _paintTool.value!!.paint.apply { this.color = color }
            )
        )
    }

    /**Method to get the color from the tool.
     * Only used in testing currently.
     *
     */
    fun getColor(): Int? {
        return paintTool.value?.paint?.color
    }

    /**Method to update the size of the paint tool
     * Size comes from the user clicking on the button and then the dialog box.
     *
     */

    fun setSize(size: Float) {
        _paintTool.postValue(
            _paintTool.value?.copy(
                paint = _paintTool.value!!.paint.apply { this.strokeWidth = size }
            )
        )
    }

    /**Method to get the size from the paint tool.
     * Currently only used in testing.
     *
     */
    fun getSize(): Float?{
       return paintTool.value?.paint?.strokeWidth
    }

    /**Method to update the style of the paint tool
     * Not currently used yet.
     *
     */

    fun setStyle(style: Paint.Style) {
        _paintTool.postValue(
            _paintTool.value?.copy(
                paint = _paintTool.value!!.paint.apply { this.style = style }
            )
        )
    }

    /**Method to update the shape of the paint tool
     * Shape comes from the user clicking on the button and then the dialog box.
     *
     */

    fun setShape(shape: String) {
        _paintTool.postValue(
            _paintTool.value?.copy(currentShape = shape)
        )
    }

    /**Method to get the shape from the paint tool.
     *Used only in testing.
     */
    fun getShape(): String?{
        return paintTool.value?.currentShape
    }

    /**Used to reset the drawing
     *
     */
    fun resetDrawing(width: Int, height: Int) {
        // Create a new blank bitmap
        val blankBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        _bitmap.postValue(blankBitmap)

        // Set the flag to indicate path should be reset
        _shouldReset.postValue(true)
    }

    // Ensure the reset flag is false when not resetting
    fun resetComplete() {
        _shouldReset.postValue(false)
    }

  //3) In the ViewModel, the method onUserDraw(x, y, event) handles the drawing logic.
 //The x and y coordinate info here was supplied to the view model by the fragment who delegated to the viewmodel to decide what logic to implement in response to user drawing from the customview
 // The ViewModel checks the currentShape property (which could be RECTANGLE, DIAMOND, or LINE) and calls the corresponding method to draw the shape.
 fun onUserDraw(x: Float, y: Float, event: MotionEvent) {
     val shape  = _paintTool.value?.currentShape
      Log.i("DrawViewModel", "3 - determines which drawing logic: $shape x: $x, y: $y event: ${event.action} ")
//     // Clone the current bitmap to avoid directly modifying the original so the orginal bitmap currently displayed to the user is left unchaged until the entire update is complete. This prevents flitcker, partial drawing and prevents concurrency issues
//     val updatedBitmap = _bitmap.value?.copy(Bitmap.Config.ARGB_8888, true)
//     val canvas = Canvas(updatedBitmap!!)  // Draw on the copied bitmap
//
     when (_paintTool.value?.currentShape) {
             "rectangle" -> drawRectangle(x, y, event)
             "diamond" -> drawDiamond(x, y, event)
             "line"-> drawLine(x, y, event)
             "free" -> drawFree(x, y, event)
         }
//                 // 6) After the drawing operation, the updated bitmap is set to the LiveData (currentBitmap.value = updatedBitmap).
//                 // This triggers an update to the observers of currentBitmap (in this case, the Fragment), which will in tern update the UI.
//                 //Once the ViewModel updates the bitmap (by setting the currentBitmap.value), the observer in the fragment is notified.

//     }

}

    //4) Drawing on the Bitmap: Each drawing method (e.g., drawRectangle(x, y, event)) modifies the bitmap based on the touch interaction.
    private fun drawFree(x: Float, y: Float, event: MotionEvent) {
        // Drawing logic for rectangle on the bitmap
        if(event.action == MotionEvent.ACTION_UP)  {
            Log.i("DrawViewModel", " 4 - (touch) execute drawing free logic$x, y: $y, event type: ${event.classification}")
            val rando = Color.rgb(Random.nextInt(256), Random.nextInt(256), Random.nextInt(256))
            Log.i("DrawViewModel", "4a - (touch) drawing a random color")
            _backendCanvas?.drawColor(rando)
            _bitmap.value = _bitmap.value
        }

    }

    private fun drawRectangle(x: Float, y: Float, event: MotionEvent) {
        // Drawing logic for rectangle on the bitmap
        Log.i("DrawViewModel", "drawing a rectangle")

    }

    private fun drawDiamond(x: Float, y: Float, event: MotionEvent) {
        // Drawing logic for diamond on the bitmap
        Log.i("DrawViewModel", "drawing a rectangle")
    }
    private fun drawLine(x: Float, y: Float, event: MotionEvent) {
        val startTime = System.currentTimeMillis()
        Log.i("DrawViewModel", "drawing a rectangle")
        val endTime = System.currentTimeMillis()
        Log.i("DrawViewModel", "Time taken for bitmap update: ${endTime - startTime} ms")
    }


//
//    fun selectShape(shape: Shape) {
//        currentShape = shape
//    }
}

//Shape mode?
//when (event.action ) {
//    MotionEvent.ACTION_DOWN -> {
//        // Handle start of touch
//        Log.i("DrawViewModel", "Action Down: Starting to draw shape: ${_paintTool.value?.currentShape}")
//    }
//    MotionEvent.ACTION_MOVE -> {
//        // Handle drawing as finger moves
//        Log.i("DrawViewModel", "Action Move: Drawing shape: ${_paintTool.value?.currentShape}")
//        when (_paintTool.value?.currentShape) {
//            "rectangle" -> drawRectangle(x, y, event)
//            "diamond" -> drawDiamond(x, y, event)
//            "line"-> drawLine(x, y, event)
//            "free" -> drawFree(x, y, event)
//        }
//    }
//    MotionEvent.ACTION_UP -> {
//        // Finalize the shape drawing
//        Log.i("DrawViewModel", "Action Up: Finalizing shape: ${_paintTool.value?.currentShape}")
//        // You can clone the bitmap here, after the drawing is complete
//    }
//}