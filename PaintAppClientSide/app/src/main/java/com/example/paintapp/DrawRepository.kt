package com.example.paintapp
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class DrawRepository(val scope: CoroutineScope, val dao: DrawDAO, val context: android.content.Context) {

    val httpClient: HttpClient by lazy {
        // sets up client which uses json objects
        HttpClient {
            install(ContentNegotiation) {
                json()
            }
        }
    }

    //TODO: update so it sends real drawing (with all data) not test drawing
    suspend fun shareWithinApp(drawing :Drawing){
        //sends drawing to sever
        val response: HttpResponse = httpClient.post("http://10.0.2.2:8080/drawing") {
            contentType(io.ktor.http.ContentType.Application.Json) //Sets the content type of the request to JSON.
            setBody(drawing) //Serializes the Book object and sets it as the request body.
        }
    }

    fun closeClient() {
        httpClient.close()
    }

//    //TODO: use http client to send GET and POST requests to server
//    private val client = (context.applicationContext as DrawApp).httpClient //No value passed for parameter 'content'

    private val _allDrawings = MutableLiveData<List<Drawing>>()
    val allDrawings: LiveData<List<Drawing>> get() = _allDrawings

    private var selectedDrawing: Drawing? = null

    // Method to get the selected drawing
    fun getSelectedDrawing(): Drawing? {
        return selectedDrawing
    }

    // Method to set the selected drawing
    fun setSelectedDrawing(drawing: Drawing) {
        selectedDrawing = drawing
    }

    //Load in all drawings at the start of the app, instead during fragment creation
    init {
        scope.launch {
            loadAllDrawings()
        }
    }

    // When app starts up, transform filenames into Drawing objects with bitmaps
    private suspend fun loadAllDrawings() {
        withContext(Dispatchers.IO) {
            // Using .first() instead of collect() in a Kotlin coroutine flow means that the flow will emit only the first value and then stop listening to further updates. This is useful when you only need a one-time retrieval of data rather than continuous updates.
            val drawingEntities = dao.getAllDrawings().first()
            //load in all bitmaps if there are any
            if (drawingEntities.isNotEmpty()) {
                val drawings = drawingEntities.map { entity ->
                    async(Dispatchers.IO) {
                        val bitmap = loadBitmapFromFile(entity.fileName) ?: defaultBitmap
                        Drawing(
                            id = entity.id,
                            bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true),
                            fileName = entity.fileName,
                            imageTitle = entity.userChosenFileName
                        )
                    }
                }.awaitAll()
                _allDrawings.postValue(drawings)
            }
            //else no drawings have been created yet (empty gallary)
            else {
                _allDrawings.postValue(emptyList())
            }
        }
    }

    //Saves the bitmap data in a file to disk, and saves the path to it in the room database
    suspend fun addDrawing(bitmap: Bitmap, userChosenFileName: String): Drawing {
        //Save bitmap to disk
        val backendFileName = "${System.currentTimeMillis()}.png"

        val file = File(context.filesDir, backendFileName) //create an empty file file in 'fileDir' special private folder only for the paint app files
        saveBitmapToFile(bitmap, file) //Add the bitmap data to this file

        //Save path in room database
        val drawEntity = DrawEntity(fileName = file.absolutePath, userChosenFileName = userChosenFileName) //Create a record (ie drawing record), with the absolute path as its field
        val id = dao.addDrawing(drawEntity) //insert into database

        //Create a Drawing object, now including the generated ID, file path, and bitmap
        val newDrawing = Drawing(
            id = id,
            bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true),//TODO: should I make this immutable?
            fileName = drawEntity.fileName,
            imageTitle = drawEntity.userChosenFileName
        )

        //Get the current list, adds the new drawing to the end of the list, updates the live data
        val currentList = _allDrawings.value.orEmpty().toMutableList()  //takes the immutable list of drawing and converts it to mutable (ie can edit)
        currentList.add(newDrawing)

        //UI won't freeze waiting for this operation to take place, just will update the main thread when ready
        _allDrawings.postValue(currentList)// uses post value to ensure thread safe if its called from background thread

        return newDrawing
    }

    suspend fun updateExistingDrawing(updatedDrawing: Drawing) {
        //updates the list the viewer sees immediately on the main thread, this means that their modifications are immediately reflected in the list they see (ie giving the illusion of an instantaneous save even though it might take time to finish saving in the background)
        withContext(Dispatchers.Main) {
            val currentList = _allDrawings.value?.toMutableList() ?: mutableListOf() //

            //Find the drawing in the list that matches this index
            val index = currentList.indexOfFirst { it.id == updatedDrawing.id }

            // Replace the old drawing with the updated one
            val updatedBitmap = updatedDrawing.bitmap?.copy(Bitmap.Config.ARGB_8888, true)
            currentList[index] = updatedDrawing.copy(bitmap = updatedBitmap)
            _allDrawings.setValue(currentList)
        }
        //saving of the file to disk continues on a background thread
        withContext(Dispatchers.IO) {
            val file = File(
                updatedDrawing.fileName
            ) //create an empty file file in 'fileDir' special private folder only for the paint app files
            updatedDrawing.bitmap?.let {
                saveBitmapToFile(
                    it,
                    file
                )
            } //gives updates to those tracking live data
        }
    }

    // Update only the file name of a specific drawing by its ID
    suspend fun updateDrawingFileName(drawingId: Long, newFileName: String) {
        //Optimistically update UI with new name
        val currentList = _allDrawings.value?.toMutableList() ?: mutableListOf() //
        //Find the drawing in the list that matches this index
        val index = currentList.indexOfFirst { it.id == drawingId }

        //save to database in the background
        withContext(Dispatchers.IO) {
            currentList[index].imageTitle = newFileName
            dao.updateFileName(drawingId, newFileName) // Directly update the database record
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

                if (file.exists()) {
                    val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                    if (bitmap != null) {
                        return@withContext bitmap.copy(Bitmap.Config.ARGB_8888, true)
                    }
                }
            } catch (e: Exception) {
                Log.e("BitmapDebug", "Error loading bitmap from file: $fileName", e)
            }
            return@withContext null
        }
    }

    //used to help with the file sharing process
    fun getDrawingUri(fullFilePath: String): Uri? {
        val file = File(fullFilePath)
        return if (file.exists()) {
            //for sharing of files from your app to other apps securely
            FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        } else {
            null
        }
    }

    private val defaultBitmap = Bitmap.createBitmap(1080, 2209, Bitmap.Config.ARGB_8888)
}

