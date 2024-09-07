package com.example.paintapp

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
data class PaintTool(
    val paint: Paint = Paint().apply {
        color = Color.BLACK
        strokeWidth = 5f
        style = Paint.Style.STROKE
        isAntiAlias = true
    },
    val shape: String = "free" // Default shape is "free draw"
)

class DrawViewModel : ViewModel() {

    // Bitmap for storing the drawing
    private val _bitmap = MutableLiveData<Bitmap?>()
    val bitmap: LiveData<Bitmap?> get() = _bitmap

    // LiveData for the PaintTool object
    private val _paintTool = MutableLiveData<PaintTool>().apply {
        value = PaintTool() // Default values
    }

    // Flag to indicate whether the drawing should be reset
    private val _shouldReset = MutableLiveData<Boolean>()
    val shouldReset: LiveData<Boolean> get() = _shouldReset
    fun setBitmap(bitmap: Bitmap?) {
        _bitmap.value = bitmap
    }
    val paintTool: LiveData<PaintTool> get() = _paintTool

    // Methods to update the PaintTool object
    fun setColor(color: Int) {
        _paintTool.value = _paintTool.value?.copy(
            paint = _paintTool.value!!.paint.apply { this.color = color }
        )
    }
    fun setSize(size: Float) {
        _paintTool.value = _paintTool.value?.copy(
            paint = _paintTool.value!!.paint.apply { this.strokeWidth = size }
        )
    }
    fun setStyle(style: Paint.Style) {
        _paintTool.value = _paintTool.value?.copy(
            paint = _paintTool.value!!.paint.apply { this.style = style }
        )
    }
    fun setShape(shape: String) {
        _paintTool.value = _paintTool.value?.copy(shape = shape)
    }

    // Reset the bitmap to a blank state
    fun resetDrawing(width: Int, height: Int) {
        // Create a new blank bitmap
        val blankBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        _bitmap.value = blankBitmap

        // Set the flag to indicate path should be reset
        _shouldReset.value = true
    }

    // Ensure the reset flag is false when not resetting
    fun resetComplete() {
        _shouldReset.value = false
    }
}
