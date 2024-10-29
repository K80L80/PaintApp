package com.example.paintapp

import android.content.Intent
import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

// MainMenuView Model handles the businesses logic of drawing gallery
class GalleryViewModel(drawRepository: DrawRepository) : ViewModel() {
    // Method to add a new drawing
    private val _drawRepository = drawRepository

    val drawings: LiveData<List<Drawing>> = drawRepository.allDrawings
    // Load all drawings once when the app starts or the menu is displayed

    //Live data to share outside of app (ie messages or
    private val _shareIntent = MutableLiveData<Intent>()
    val shareIntent: LiveData<Intent> get() = _shareIntent

    // LiveData to share within app to other paint drawing users
    private val _sharedDrawing = MutableLiveData<Drawing>()
    val sharedDrawing: LiveData<Drawing> get() = _sharedDrawing

    // To display a list of drawing available for download
    private val _drawingList = MutableLiveData<List<Drawing>>()
    val drawingList: LiveData<List<Drawing>> get() = _drawingList


    fun selectDrawing(drawing: Drawing) {
        _drawRepository.setSelectedDrawing(drawing)
    }

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
    fun shareDrawingOutsideApp(fileName: String) {//ViewModel prepares the sharing intent (e.g., using a file URI) and posts it to the LiveData.
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

    fun getDrawingList(userID: String) {
        viewModelScope.launch {
            val drawings = _drawRepository.getDrawingList(userID)
            _drawingList.value = drawings  // Update LiveData with the fetched list
        }
    }

    // Method to add a shared drawing
    fun shareWithinApp(drawing: Drawing) {
        viewModelScope.launch {
            _drawRepository.shareWithinApp(drawing)
        }
    }

    fun downloadDrawing(drawing: Drawing){
        viewModelScope.launch {
            _drawRepository.downloadDrawing(drawing)
        }
    }

    suspend fun importDrawImage(fileName: String){
        _drawRepository.loadBitmapFromFile(fileName)
    }

    suspend fun getDrawingsList(): List<Drawing> {
        return _drawRepository.getAllDrawingsFromServer()
    }
}