/**Draw view model class for handling our live data and paint data class.
 * Date:09/12/2024
 *
 */
package com.example.paintapp

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
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
        strokeWidth = 30f
    },
   // val color: Int = Color.BLACK,
    val currentShape: String = "free" // Default shape is "free draw"
    //TODO: Add mode? Free?
)
data class Drawing(
    val id: Long, // Unique identifier for the drawing
    val bitmap: Bitmap, // Bitmap for storing the drawing
    val fileName: String
)

//'Backend canvas' in the ViewModel treated as a tool for updating the bitmap
class DrawViewModel : ViewModel() {
    private val _drawings = MutableLiveData<List<Drawing>>()
    val drawings: LiveData<List<Drawing>> get() = _drawings

    private val _selectedDrawing = MutableLiveData<Drawing?>()
    val selectedDrawing: LiveData<Drawing?> get() = _selectedDrawing

    // Backend canvas to modify the bitmap
    private var _backendCanvas: Canvas? = null
    private var freeDrawPath: Path = Path()  // Path to hold the freehand drawing

    // Initialize with some mock data or load from repository
    init {
        _drawings.value = generateTestDrawings()
    }

    // Method to select a drawing
    fun selectDrawing(drawing: Drawing) {
        _selectedDrawing.value = drawing
        _backendCanvas = Canvas(drawing.bitmap)
    }
    // Method to add a new drawing
    fun addNewDrawing(newDrawing: Drawing) {
        val currentDrawings = _drawings.value ?: emptyList()
        _drawings.value = currentDrawings + newDrawing
    }

    // Inside your DrawViewModel
    fun createNewDrawing() {
        val newBitmap = Bitmap.createBitmap(800, 800, Bitmap.Config.ARGB_8888) // Create a blank bitmap
        val newDrawing = Drawing(id = System.currentTimeMillis(), bitmap = newBitmap, fileName = "new_drawing")

        val currentDrawings = _drawings.value ?: emptyList()
        _drawings.value = currentDrawings + newDrawing

        // Set the new drawing as the selected drawing
        _selectedDrawing.value = newDrawing
    }
    fun updateDrawingInList(updatedDrawing: Drawing) {
       
        val currentList = _drawings.value?.toMutableList() ?: mutableListOf() // //Retrieves the current list of drawings from _drawings (which is a LiveData object) and converts it to a mutable list (a list that can be changed).
        //Find the drawing in the list that matches this index
        val index = currentList.indexOfFirst { it.id == updatedDrawing.id }

        if (index != -1) { //make sure drawing with that index is in the list, returns negative if not present
            // Replace the old drawing with the updated one
            currentList[index] = updatedDrawing // Update the drawing in the list
            _drawings.value = currentList // Update the LiveData list
        }
    }


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
        //val currentBitmap = _bitmap.value //before
        val currentBitmap =  _selectedDrawing.value?.bitmap
        //create new drawing for user
        if (currentBitmap == null) {
            Log.i("ViewModel", "4a new bitmap was created!")
            // bitmap.value is null create a new one, make a new user bitmap to store a drawing, that matches the current height and
            val createdBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888)
            _backendCanvas = Canvas(createdBitmap)
            Log.i("ViewModel", "4b load backend canvas with new bitmap")
            //_bitmap.value = createdBitmap //before
            _selectedDrawing.value = _selectedDrawing.value?.copy(bitmap = createdBitmap) //Val cannot be reassigned
        }
        //restore old user drawing (the bitmap) and resize if needed
        else {
                Log.i("ViewModel", "4c restore old bitmap")
            if (currentBitmap.width != newWidth || currentBitmap.height != newHeight) {
                Log.i("ViewModel", "4d bitmap needs to be scaled!")
                val scaledBitmap =
                    Bitmap.createScaledBitmap(currentBitmap, newWidth, newHeight, true)
                //_bitmap.value = scaledBitmap //before
                _selectedDrawing.value = _selectedDrawing.value?.copy(bitmap = scaledBitmap) ////after
                Log.i("ViewModel", "4e load backend canvas with scaled bitmap ")
                _backendCanvas = Canvas(scaledBitmap) //
            } else {
                Log.i("ViewModel", "4f The existing bitmap was restored, no resizing needed")
            }
        }
    }

    /**Setter for the bitmap
     *
     */
    fun setBitmap(bitmap: Bitmap?) {
       // _bitmap.postValue(bitmap)
        bitmap?.let {
            _selectedDrawing.value = _selectedDrawing.value?.copy(bitmap = bitmap)//after
        }
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
        Log.i("DrawViewModel", "size of paint tool set")
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
    fun getSize(): Float? {
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
        Log.i("ViewModel", "setting shape ")
        _paintTool.postValue(
            _paintTool.value?.copy(currentShape = shape)
        )
    }

    /**Method to get the shape from the paint tool.
     *Used only in testing.
     */
    fun getShape(): String? {
        return paintTool.value?.currentShape
    }

    /**Used to reset the drawing
     *
     */
    fun resetDrawing(width: Int, height: Int) {
        // Create a new blank bitmap
        val blankBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        _selectedDrawing.value = _selectedDrawing.value?.copy(bitmap = blankBitmap)

        // Set the flag to indicate path should be reset
        _shouldReset.postValue(true)
    }

    // Ensure the reset flag is false when not resetting
    fun resetComplete() {
        _shouldReset.postValue(false)
    }

    // In the ViewModel, the method onUserDraw(x, y, event) handles the drawing logic.
    // The x and y coordinate info here was supplied to the view model by the fragment who delegated to the viewmodel to decide what logic to implement in response to user drawing from the customview
    // The ViewModel checks the currentShape property (which could be RECTANGLE, DIAMOND, or LINE) and calls the corresponding method to draw the shape.
    fun onUserDraw(x: Float, y: Float, event: MotionEvent) {
        val shape = _paintTool.value?.currentShape
        Log.i(
            "DrawViewModel",
            "3 - determines which drawing logic: $shape x: $x, y: $y event: ${event.action} "
        )

        //if its just a normal shape
        if (shape != "free" && (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE)) {
            Log.i("DrawViewModel", "$x, y: $y, event type: ${event.classification}")
            when (_paintTool.value?.currentShape) {
                "free" -> drawFree(x, y, event)
                "line" -> drawLine(x, y, event)
                "circle" -> drawCircle(x, y, event)
                "square" -> drawSquare(x, y, event)
                "rectangle" -> drawRectangle(x, y, event)
                "diamond" -> drawDiamond(x, y, event)
            }
        }
        //free draw requires processing of up down ect.
        else {
            drawFree(x, y, event)
        }
    }

    // After the drawing operation, the updated bitmap is set to the LiveData (currentBitmap.value = updatedBitmap).
    // This triggers an update to the observers of currentBitmap (in this case, the Fragment), which will in tern update the UI.
    //Once the ViewModel updates the bitmap (by setting the currentBitmap.value), the observer in the fragment is notified.
    //Drawing on the Bitmap: Each drawing method (e.g., drawRectangle(x, y, event)) modifies the bitmap based on the touch interaction.
    private fun drawSquare(x: Float, y: Float, event: MotionEvent) {
        // Drawing logic for rectangle on the bitmap
        Log.i("DrawViewModel", "drawing a square")
        // Drawing logic for rectangle on the bitmap

        val width: Float = this.getSize() ?: 30f //gets stroke width
        val height: Float = this.getSize() ?: 30f //gets stroke width

        Log.i("DrawViewModel", "4a - (touch) drawing a random color")
        _paintTool.value?.let {
            _backendCanvas?.drawRect(
                x + width / 2,
                y + height / 2,
                x - width / 2,
                y - height / 2,
                it.paint
            )
            _selectedDrawing.value?.bitmap?.let { bitmap ->
                _selectedDrawing.value = _selectedDrawing.value?.copy(bitmap = bitmap)
            }
        }
    }

    private fun drawCircle(x: Float, y: Float, event: MotionEvent) {
        // Drawing logic for rectangle on the bitmap
        val radius: Float = this.getSize() ?: 30f
        Log.i("DrawViewModel", "drawing a circle")
        _paintTool.value?.let {
            Log.i("DrawViewModel", "drawing a circle")
            _backendCanvas?.drawCircle(x, y, radius, it.paint)
            _selectedDrawing.value?.bitmap?.let { bitmap ->
                _selectedDrawing.value = _selectedDrawing.value?.copy(bitmap = bitmap)
            }
        }
    }

    private fun drawRectangle(x: Float, y: Float, event: MotionEvent) {
        // Drawing logic for rectangle on the bitmap
        val width: Float = this.getSize() ?: 30f //gets stroke width
        val height: Float = this.getSize() ?: 30f //gets stroke width

        val scaledUpHeight = height * 2
        Log.i("DrawViewModel", "drawing a rectangle")
        _paintTool.value?.let {
            _backendCanvas?.drawRect(
                x + width / 2,
                y + scaledUpHeight / 2,
                x - width / 2,
                y - scaledUpHeight / 2,
                it.paint
            )
            _selectedDrawing.value?.bitmap?.let { bitmap ->
                _selectedDrawing.value = _selectedDrawing.value?.copy(bitmap = bitmap)
            }
        }

    }

    private var startX = 0f
    private var startY = 0f

    private fun drawLine(x: Float, y: Float, event: MotionEvent) {
        _paintTool.value?.let {
            // Create a copy of the Paint object
            val copiedPaint = Paint(it.paint).apply {
                style = Paint.Style.STROKE
            }

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Capture the starting point of the line
                    startX = x
                    startY = y
                }

                MotionEvent.ACTION_MOVE -> {
                    // (Optional) Preview the line while dragging
                    // Redraw the view without committing it to the bitmap
                    _backendCanvas?.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR) // Clear the preview
                    _backendCanvas?.drawLine(startX, startY, x, y, copiedPaint)
                    _selectedDrawing.value?.bitmap?.let { bitmap ->
                        _selectedDrawing.value = _selectedDrawing.value?.copy(bitmap = bitmap)
                    }
                }

                MotionEvent.ACTION_UP -> {
                    // Finalize the line when the user lifts their finger and commit to the bitmap
                    _backendCanvas?.drawLine(startX, startY, x, y, copiedPaint)
                    _selectedDrawing.value?.bitmap?.let { bitmap ->
                        _selectedDrawing.value = _selectedDrawing.value?.copy(bitmap = bitmap)
                    }
                }
                else -> {
                    // Handle any other unexpected motion events, or just ignore them
                    Log.w("DrawViewModel", "Unhandled MotionEvent action: ${event.action}")
                }
            }
        }
    }

    //must use path for non-uniform shapes (ie circle, rectangle)
    private fun drawDiamond(x: Float, y: Float, event: MotionEvent) {
        // Drawing logic for diamond on the bitmap
        Log.i("DrawViewModel", "drawing a diamond")

        // Size of the diamond
        val size: Float = this.getSize() ?: 30f
        val halfSize = size / 2

        // Create a new Path for the diamond
        val path = Path().apply {
            // Move to the top point of the diamond
            moveTo(x, y - halfSize)  // Top point

            // Draw line to the right point
            lineTo(x + halfSize, y)  // Right point

            // Draw line to the bottom point
            lineTo(x, y + halfSize)  // Bottom point

            // Draw line to the left point
            lineTo(x - halfSize, y)  // Left point

            // Close the path back to the top point
            close()  // This connects the last point to the first point
        }

        // Get the current paint tool and draw the diamond on the canvas
        _paintTool.value?.let {
            _backendCanvas?.drawPath(path, it.paint)
        }
    }

    private fun drawFree(x: Float, y: Float, event: MotionEvent) {
        _paintTool.value?.let {
            // Create a copy of the Paint object
            val copiedPaint = Paint(it.paint).apply {
                style = Paint.Style.STROKE
            }

            when (event.action) { //now saying 'when' expression must be exhaustive, add necessary 'else' branch
                MotionEvent.ACTION_DOWN -> {
                    // Start a new path at the touch down point
                    freeDrawPath.moveTo(x, y)
                }

                MotionEvent.ACTION_MOVE -> {
                    // Continue the path as the user moves their finger
                    freeDrawPath.lineTo(x, y)

                    // Draw the path in real-time as the user is drawing (during ACTION_MOVE)
                    _backendCanvas?.drawPath(freeDrawPath, copiedPaint)

                    // Redraw the canvas with the updated path
                    _selectedDrawing.value?.bitmap?.let { bitmap ->
                        _selectedDrawing.value = _selectedDrawing.value?.copy(bitmap = bitmap)
                    }
                }

                MotionEvent.ACTION_UP -> {
                    // Finalize the path when the user lifts their finger
                    _backendCanvas?.drawPath(freeDrawPath, copiedPaint)

                    _selectedDrawing.value?.bitmap?.let { bitmap ->
                        _selectedDrawing.value = _selectedDrawing.value?.copy(bitmap = bitmap)
                    }

                    // Reset the path for the next freehand stroke
                    freeDrawPath.reset()
                }
                else -> {
                    // Handle any other unexpected motion events, or just ignore them
                    Log.w("DrawViewModel", "Unhandled MotionEvent action: ${event.action}")
                }
            }
        }
    }

    fun clearBitmap() {
        _selectedDrawing.value?.let {
            // Clear the existing bitmap by drawing a transparent background
            val canvas = Canvas(it.bitmap)
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)  // Clear the bitmap
            _selectedDrawing.value = it  // Post the cleared bitmap
        }
    }
}




