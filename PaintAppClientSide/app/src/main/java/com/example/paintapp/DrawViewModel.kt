/**Draw view model class for handling our live data and paint data class.
 * Date:09/12/2024
 *
 */
package com.example.paintapp

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import android.view.MotionEvent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.Flow


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

//'Backend canvas' in the ViewModel treated as a tool for updating the bitmap
class DrawViewModel(drawRepository: DrawRepository) : ViewModel() {
    // Method to add a new drawing
    private val _drawRepository = drawRepository
    private lateinit var sensorManager: SensorManager
    private lateinit var accelerometer: Sensor

    val drawings: LiveData<List<Drawing>> = drawRepository.allDrawings
    // Load all drawings once when the app starts or the menu is displayed

    //selected Drawing is session based (ie selected drawing does not need to be tracked in database since you always have to pick you drawing through main app and that is a decision relative to the current instance not across app instances)
    private var _selectedDrawing = MutableLiveData<Drawing?>()
    val selectedDrawing: LiveData<Drawing?> get() = _selectedDrawing

    // Backend canvas to modify the bitmap
    private var _backendCanvas: Canvas? = null
    private var freeDrawPath: Path = Path()  // Path to hold the freehand drawing

    //for drawing sharing
    private val _shareIntent = MutableLiveData<Intent>()
    val shareIntent: LiveData<Intent> get() = _shareIntent

    // LiveData for the PaintTool object
    private val _paintTool = MutableLiveData<PaintTool>().apply {
        // Use postValue to ensure it's safe for background threads
        postValue(PaintTool())
    }
    val paintTool: LiveData<PaintTool> get() = _paintTool

    // Flag to indicate whether the drawing should be reset
    private val _shouldReset = MutableLiveData<Boolean>()

    // Load the selected drawing from the repository
    fun loadSelectedDrawing() {
        val drawing = _drawRepository.getSelectedDrawing()
        _selectedDrawing.value = drawing
        drawing?.let {
            _backendCanvas =
                it.bitmap?.let { it1 -> Canvas(it1) } //hook up selected drawing to backend canvas used for modifying the bitmap
        }
    }

    //when user navigates away from the draw screen or clicks save, this will take their drawing and save any changes they made (so then when gallery is rendered they see their most recent updates in the thumbnail of their drawing)
    fun saveCurrentDrawing(newBitmap: Bitmap) {
        // takes the user drawing (that they modified) and saves the changes to the List of drawings
        _selectedDrawing.value?.let { drawing ->
            val updatedDrawing = drawing.copy(bitmap = newBitmap)
            _selectedDrawing.value = updatedDrawing

            viewModelScope.launch {
                _drawRepository.updateExistingDrawing(updatedDrawing)
            }
        }
    }

    fun respondToResizeEvent(newWidth: Int, newHeight: Int) {
        Log.i("ViewModel", "respond to resize event${_selectedDrawing.value?.bitmap}")

        val currentBitmap = _selectedDrawing.value?.bitmap ?: return

        if (currentBitmap.width != newWidth || currentBitmap.height != newHeight) {
            val scaledBitmap = Bitmap.createScaledBitmap(currentBitmap, newWidth, newHeight, true)
            _selectedDrawing.value = _selectedDrawing.value?.copy(bitmap = scaledBitmap) ////after
            Log.i("ViewModel", "rescaled bitmap ${_selectedDrawing.value?.bitmap}")
            _backendCanvas = Canvas(scaledBitmap)
        } else {
            Log.i("ViewModel", " no resizing needed bitmap is: ${_selectedDrawing.value?.bitmap}")
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
        val width: Float = this.getSize() ?: 30f //gets stroke width
        val height: Float = this.getSize() ?: 30f //gets stroke width

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
        _paintTool.value?.let {
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
                    _backendCanvas?.drawColor(
                        Color.TRANSPARENT,
                        PorterDuff.Mode.CLEAR
                    ) // Clear the preview
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
            it.bitmap?.let { it1 -> Canvas(it1) }
                ?.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)  // Clear the bitmap
            _selectedDrawing.value = it  // Post the cleared bitmap

        }
    }

    // for sharing a drawing,
    fun exportDrawing() {//ViewModel prepares the sharing intent (e.g., using a file URI) and posts it to the LiveData.
        val drawingUri = _selectedDrawing.value?.let { _drawRepository.getDrawingUri(it.fileName) }
        drawingUri?.let {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, drawingUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            _shareIntent.postValue(Intent.createChooser(intent, "Share Drawing"))
        }
    }

    /**Driver to send boolean of isShaking to the drawFragment
     *
     */
    fun shakeDetector(accelerometer: Sensor, sensorManager: SensorManager): LiveData<Boolean> {
        return liveData {
            shakeDetectorFlow(accelerometer, sensorManager).collect { shaking ->
                emit(shaking)
            }
        }
    }

    /**Method to check for changes in positionals for shaking
     *
     */
    private fun shakeDetectorFlow(
        accelerometer: Sensor,
        sensorManager: SensorManager
    ): Flow<Boolean> {
        return channelFlow {
            val listener = object : SensorEventListener {
                val history = mutableListOf<FloatArray>()

                override fun onSensorChanged(event: SensorEvent?) {
                    if (event != null) {
                        if (history.size > 10) {
                            history.removeAt(0)
                        }
                        history.add(event.values.copyOf())

                        val minDotProduct = history.minOf {
                            it.zip(event.values).map { pair -> pair.first * pair.second }
                                .reduce(Float::plus)
                        }

                        channel.trySend(minDotProduct < -2.0)
                    }
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
            }

            sensorManager.registerListener(
                listener,
                accelerometer,
                SensorManager.SENSOR_DELAY_NORMAL
            )

            awaitClose {
                sensorManager.unregisterListener(listener)
            }
        }
    }

    /**Method to increase size
     * Helper for shake
     *
     */
    fun increaseSize() {
        val currentSize = getSize() ?: 10f
        if (currentSize < 45f) {
            setSize(currentSize + 5f)
        }
    }


    fun createDefaultDrawing(id: Long, width: Int, height: Int, fileName: String): Drawing {
        // Create an empty bitmap
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        // Initialize a canvas to draw on the bitmap
        val canvas = Canvas(bitmap)

        // Set background color
        canvas.drawColor(Color.LTGRAY)

        // Set up paint for text
        val paint = Paint().apply {
            color = Color.RED
            textSize = 40f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
        }

        // Draw the error message on the bitmap
        val message = "Something went wrong with this drawing"
        canvas.drawText(message, (width / 2).toFloat(), (height / 2).toFloat(), paint)

        // Return a Drawing object with this bitmap and filename
        return Drawing(
            id = id,
            bitmap = bitmap,
            fileName = fileName,
            imageTitle = "untitled",
            ownerID = _drawRepository.getuID()

        )
    }

    val defaultDrawing = createDefaultDrawing(
        id = -1L, // Assign an invalid id or one that represents the default
        width = 800, // Specify a default width
        height = 600, // Specify a default height
        fileName = "error_drawing.png"
    )
}
