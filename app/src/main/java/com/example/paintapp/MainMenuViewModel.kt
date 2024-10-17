package com.example.paintapp

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

// Method to select a drawing (ie local reference to the drawing the user picked from the main menu that they want to now modify)
//'Backend canvas' in the ViewModel treated as a tool for updating the bitmap
class MainMenuViewModel(drawRepository: DrawRepository) : ViewModel() {
    // Method to add a new drawing
    private val _drawRepository = drawRepository

    val drawings: LiveData<List<Drawing>> = drawRepository.allDrawings
    // Load all drawings once when the app starts or the menu is displayed

    private val _shareIntent = MutableLiveData<Intent>()
    val shareIntent: LiveData<Intent> get() = _shareIntent

    fun selectDrawing(drawing: Drawing) {
        _drawRepository.setSelectedDrawing(drawing)
    }

    //        // Only copy the bitmap to mutable once, if it's not already mutable
//        val mutableBitmap = if (!drawing.bitmap.isMutable) {
//            drawing.bitmap.copy(Bitmap.Config.ARGB_8888, true)
//        } else {
//            drawing.bitmap
//        }

    //Method called when user clicks 'new drawing' on main menu and taken to blank screen to draw some new stuff (this method creates a drawing object and updates the repository and local references ('_selectedDrawing' and '_backendCanvas') needed to modify underlying bitmap
    fun createNewDrawing(fileName: String?) {
        val newBitmap =
            Bitmap.createBitmap(1080, 2209, Bitmap.Config.ARGB_8888) // Create a blank bitmap

        //adds new drawing to list backed by repo
        viewModelScope.launch {
            val userChosenFileName = fileName ?: "untitled"
            val newDrawing = async { _drawRepository.addDrawing(newBitmap, userChosenFileName) }
            // Set the 'new drawing' as the selected drawing (local reference to the draw the user picked to draw on)
            selectDrawing(newDrawing.await()) //hooks up
        }
    }

    // Update the file name and refresh the UI
    fun updateDrawingFileName(drawingId: Long, newFileName: String) {
        viewModelScope.launch {
            // Update the database in the background
            _drawRepository.updateDrawingFileName(drawingId, newFileName)
        }
    }

    // for sharing a drawing,
    fun shareDrawing(fileName: String) {//ViewModel prepares the sharing intent (e.g., using a file URI) and posts it to the LiveData.
        val drawingUri = _drawRepository.getDrawingUri(fileName)
        drawingUri?.let {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, drawingUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            _shareIntent.postValue(Intent.createChooser(intent, "Share Drawing"))
        }
    }
}