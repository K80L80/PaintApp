package com.example.paintapp

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

//TODO: pass DOA 'private val drawDao: DrawDAO'
//TODO: eventually turn this to 'val allDrawings = drawDao.getAllDrawings()' but for testing sake leave it as is
//val allDrawings: LiveData<List<Drawing>> get() =  generateTestDrawingsAsLiveData()
// For testing: replace with actual DAO later

class DrawRepository(val scope: CoroutineScope, val dao: DrawDAO, val context: android.content.Context) {

    private val _allDrawings = MutableLiveData<List<Drawing>>()
    val allDrawings: LiveData<List<Drawing>> get() = _allDrawings

    //Load in all drawings at the start of the app, instead during fragment creation
    init {
        scope.launch {
            loadAllDrawings()
        }
    }

    // When app starts up, transform filenames into Drawing objects with bitmaps
    suspend fun loadAllDrawings() {
        withContext(Dispatchers.IO) {
            Log.d("Repository", "loadAllDrawings() called")

            dao.getAllDrawings().collect { drawingEntities ->
                Log.d("Repository", "${drawingEntities.size} entities found")

                if (drawingEntities.isNotEmpty()) {
                    val drawings = drawingEntities.map { entity ->
                        val bitmap = loadBitmapFromFile(entity.fileName) ?: defaultBitmap
                        Log.d("Repository", "loading id = ${entity.id}, bitmap = $bitmap, fileName = ${entity.fileName}")
                        Drawing(id = entity.id, bitmap = bitmap, fileName = entity.fileName)
                    }
                    _allDrawings.postValue(drawings)
                } else {
                    Log.d("Repository", "No drawing entities found, posting empty list.")
                    _allDrawings.postValue(emptyList())
                }
            }
        }
    }

    //Saves the bitmap data in a file to disk, and saves the path to it in the room database
    suspend fun addDrawing(bitmap: Bitmap, fileName: String = "${System.currentTimeMillis()}.png"): Drawing{

        Log.e("Repository", "${bitmap}, filName${fileName}")
        //Save bitmap to disk
        val file = File(context.filesDir, fileName) //create an empty file file in 'fileDir' special private folder only for the paint app files
        saveBitmapToFile(bitmap, file) //Add the bitmap data to this file

        //Save path in room database
        val drawEntity = DrawEntity(fileName = file.absolutePath) //Create a record (ie drawing record), with the absolute path as its field
        val id = dao.addDrawing(drawEntity) //insert into database

        //Create a Drawing object, now including the generated ID, file path, and bitmap
        val newDrawing = Drawing(id = id, bitmap = bitmap, fileName= drawEntity.fileName)

        //Get the current list, adds the new drawing to the end of the list, updates the live data
        val currentList = _allDrawings.value.orEmpty().toMutableList()  //takes the immutable list of drawing and converts it to mutable (ie can edit)
        currentList.add(newDrawing)

        //UI won't freeze waiting for this operation to take place, just will update the main thread when ready
        _allDrawings.postValue(currentList )// uses post value to ensure thread safe if its called from background thread

        return newDrawing
    }


    suspend fun updateExistingDrawing(updatedDrawing: Drawing){
        //updates the list the viewer sees immediately on the main thread, this means that their modifications are immediately reflected in the list they see (ie giving the illusion of an instantaneous save even though it might take time to finish saving in the background)
        withContext(Dispatchers.Main) {
            val currentList = _allDrawings.value?.toMutableList() ?: mutableListOf() //

            //Find the drawing in the list that matches this index
            val index = currentList.indexOfFirst { it.id == updatedDrawing.id }

            // Replace the old drawing with the updated one
            currentList[index] = updatedDrawing // Update the drawing in the list

            _allDrawings.postValue(currentList)
        }
        //saving of the file to disk continues on a background thread
        withContext(Dispatchers.IO){
            val file = File(
                updatedDrawing.fileName
            ) //create an empty file file in 'fileDir' special private folder only for the paint app files
            saveBitmapToFile(updatedDrawing.bitmap, file)
            //gives updates to those tracking live data
        }
    }

    //save bitmap data in special private folder designated for app
    // Save bitmap to a file in the app's private folder
    private fun saveBitmapToFile(bitmap: Bitmap, file: File) {
        file.outputStream().use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
    }

    private suspend fun loadBitmapFromFile(fileName: String): Bitmap? {
        return withContext(Dispatchers.IO) {
            try {
                val file = File(fileName)
                if (file.exists() && file.isFile) {
                    BitmapFactory.decodeFile(file.absolutePath)
                } else {
                    Log.e("Repository", "File does not exist: $fileName")
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