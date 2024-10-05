package com.example.paintapp

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

//TODO: pass DOA 'private val drawDao: DrawDAO'
//TODO: eventually turn this to 'val allDrawings = drawDao.getAllDrawings()' but for testing sake leave it as is
//val allDrawings: LiveData<List<Drawing>> get() =  generateTestDrawingsAsLiveData()
// For testing: replace with actual DAO later

class DrawRepository(val scope: CoroutineScope, val dao: DrawDAO, val context: android.content.Context) {

    private val _allDrawings = MutableLiveData<List<Drawing>>()
    val allDrawings: LiveData<List<Drawing>> get() = _allDrawings

    init {
        // Initialize with some test data or empty list
        _allDrawings.value = generateTestDrawings()

    }

    // When app starts up, transform filenames into Drawing objects with bitmaps
    suspend fun loadAllDrawings() {
        withContext(Dispatchers.IO) {
            val drawingEntities = dao.getAllDrawings().value.orEmpty()
            val drawings = drawingEntities.map { entity ->
                val bitmap = loadBitmapFromFile(entity.fileName) ?: defaultBitmap
                Drawing(id = entity.id, bitmap = bitmap, fileName = entity.fileName)
            }
            _allDrawings.postValue(drawings)
        }
    }

    suspend fun addDrawing(newDrawing: Drawing){
        //TODO: refactor to integrate doa
        //Get the current list, adds the new drawing to the end of the list, updates the live data
        val currentList = _allDrawings.value.orEmpty().toMutableList()  //takes the immutable list of drawing and converts it to mutable (ie can edit)
        currentList.add(newDrawing)
        //UI won't freeze waiting for this operation to take place, just will update the main thread when ready
        _allDrawings.postValue(currentList )// uses post value to ensure thread safe if its called from background thread
    }

    suspend fun updateExistingDrawing(updatedDrawing: Drawing ){

        val currentList = _allDrawings.value?.toMutableList() ?: mutableListOf() //

        //Find the drawing in the list that matches this index
        val index = currentList.indexOfFirst { it.id == updatedDrawing.id }

        // Replace the old drawing with the updated one
        currentList[index] = updatedDrawing // Update the drawing in the list

        //gives updates to those tracking live data
        _allDrawings.postValue(currentList)
    }

    //save bitmap data in special private folder designated for app
    // Save bitmap to a file in the app's private folder
    private suspend fun saveBitmapToFile(drawing: Drawing): File {
        return withContext(Dispatchers.IO) {
            // This is the private folder designated for your app
            val directory = context.filesDir  //before using filesDir 'special private folder designated for app I need to setup app class so I can get the app context
            val file = File(directory, "${drawing.id}.png")

            // Save the bitmap to the file
            val outputStream = file.outputStream()
            drawing.bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.close()

            file
        }
    }
    private suspend fun loadBitmapFromFile(fileName: String): Bitmap? {
        return withContext(Dispatchers.IO) {
            try {
                val file = File(context.filesDir, fileName)
                if (file.exists()) {
                    return@withContext BitmapFactory.decodeFile(file.absolutePath)
                } else {
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    val defaultBitmap = Bitmap.createBitmap(1080, 2209, Bitmap.Config.ARGB_8888)
}