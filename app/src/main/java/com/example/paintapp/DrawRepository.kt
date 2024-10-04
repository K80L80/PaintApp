package com.example.paintapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope

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

    suspend fun addDrawing(newDrawing: Drawing){
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
//TODO: before uusing filesDir 'special private folder designated for app I need to setup app class so I can get the app context
//    // Save bitmap to a file in the app's private folder
//    private suspend fun saveBitmapToFile(drawing: Drawing): File {
//        return withContext(Dispatchers.IO) {
//            // This is the private folder designated for your app
//            val directory = context.filesDir  //Unresolved reference: filesDir what import do I need?
//            val file = File(directory, "${drawing.id}.png")
//
//            // Save the bitmap to the file
//            val outputStream = file.outputStream()
//            drawing.bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
//            outputStream.close()
//
//            file
//        }
//    }
}