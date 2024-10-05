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
            println("Repository: loadAllDrawings() - ViewModel scope launching")  // Debug print statement
            val drawingEntities = dao.getAllDrawings().value.orEmpty()
            println("Repository: Retrieved drawing entities: ${drawingEntities.size} entities found")  // Debug statement

            //load in all bitmaps if there are any
            if(drawingEntities.isNotEmpty()) {
                val drawings = drawingEntities.map { entity ->
                    println("Repository: Loading bitmap for file: ${entity.fileName}")  // Debug statement
                    val bitmap = loadBitmapFromFile(entity.fileName) ?: defaultBitmap
                    Drawing(id = entity.id, bitmap = bitmap, fileName = entity.fileName)
                }
                _allDrawings.postValue(drawings)
            }
            //else no drawings have been created yet (empty gallary)
            else{
                println("Repository: No drawing entities found, posting empty list.")  // Debug statement
                _allDrawings.postValue(emptyList())
            }
        }
    }

    //Saves the bitmap data in a file to disk, and saves the path to it in the room database
    suspend fun addDrawing(bitmap: Bitmap, fileName: String = "${System.currentTimeMillis()}.png"): Drawing{

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
    private fun saveBitmapToFile(bitmap: Bitmap, file: File) {
        file.outputStream().use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
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