package com.example.paintapp
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.onUpload
import io.ktor.client.request.accept

import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType

//for sending drawing file
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.readBytes

import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

//import io.ktor.http.ContentType.Application.Json
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class DrawRepository(private val scope: CoroutineScope, private val dao: DrawDAO, val context: android.content.Context) {

    private val httpClient: HttpClient by lazy {
        // sets up client which uses json objects
        HttpClient {
            install(ContentNegotiation) {
                json() //this one uses import io.ktor.serialization.kotlinx.json.json
            }
        }
    }

    //TODO: update so it sends real drawing (with all data) not test drawing
    //To send file meta data as JSON with a file in a single request in Ktor using multipart/form-data request
    suspend fun shareWithinApp(drawing :Drawing){
        val response: HttpResponse = httpClient.post("http://10.0.2.2:8080/upload") {
            //sends Drawing as two parts 1) JSON object containing id, imageTitle, fileName 2) file itself

            setBody(MultiPartFormDataContent(
                formData {
                    //adds meta-data to be sent to server
                    append("DrawingID", "${drawing.id}")
                    append("ImageTitle", drawing.imageTitle)
                    append("fileName", drawing.fileName)
                    append("ownerID", drawing.ownerID)

                    //attaches image file to be sent to server
                    append("image", File(drawing.fileName).readBytes(), Headers.build {
                        append(HttpHeaders.ContentType, "image/png")
                        append(HttpHeaders.ContentDisposition, "filename=image${drawing.id}.png") //how the server will label the file regardless of the name on disk
                    })
                },
                boundary = "WebAppBoundary"
            )
            )
            //Callback functions to monitor progress of upload in real time
            onUpload { bytesSentTotal, contentLength ->
                println("Sent $bytesSentTotal bytes from $contentLength")
            }
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

    private var  uId: String = ""

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

            //retrieve all rows from the room data base (drawing id, image title ect)
            val drawingEntities = dao.getAllDrawings().first() // // Using .first() instead of collect() in a Kotlin coroutine flow means that the flow will emit only the first value and then stop listening to further updates. This is useful when you only need a one-time retrieval of data rather than continuous updates.

            //use meta data stored in room to load in bitmap from file and set the bitmap field
            if (drawingEntities.isNotEmpty()) {
                val drawings = drawingEntities.map { drawing ->
                    async(Dispatchers.IO) {
                        val bitmap = loadBitmapFromFile(drawing.fileName) ?: defaultBitmap
                        drawing.copy(bitmap = bitmap) //Val cannot be reassigned
                    }
                //Wait till all files are read in before posting it for the UI to display
                }.awaitAll() //The parallel loading with async-await speeds up the bitmap loading process
                _allDrawings.postValue(drawings)
            }
            //else no drawings have been created yet (empty gallery)
            else {
                _allDrawings.postValue(emptyList())
            }
        }
    }

    //Inserting a new drawing into the database while also saving the bitmap as a PNG file on disk.
    suspend fun addDrawing(bitmap: Bitmap, imageTitle: String): Drawing {

        //Save bitmap to disk
        val backendFileName = "${System.currentTimeMillis()}.png"

        val file = File(context.filesDir, backendFileName) //create an empty file file in 'fileDir' special private folder only for the paint app files
        saveBitmapToFile(bitmap, file) //Add the bitmap data to this file

        //TODO: change owner ID to actual owner ID
        //Save path in room database
        val drawing = Drawing(fileName = file.absolutePath, imageTitle = imageTitle, ownerID = "spencer2@gmail.com") //Create a record (ie drawing record), with the absolute path as its field
        val id = dao.addDrawing(drawing) //insert into database

        //Create a Drawing object, now including the generated ID, file path, and bitmap
        val drawingWBitmap = drawing.copy(id = id, bitmap = bitmap)

        //Get the current list, adds the new drawing to the end of the list, updates the live data
        val currentList = _allDrawings.value.orEmpty().toMutableList()  //takes the immutable list of drawing and converts it to mutable (ie can edit)
        currentList.add(drawingWBitmap)

        //UI won't freeze waiting for this operation to take place, just will update the main thread when ready
        _allDrawings.postValue(currentList)// uses post value to ensure thread safe if its called from background thread

        return drawingWBitmap
    }

    //Updates the details of an existing drawing, including updating the bitmap on disk if it has changed.
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
            val file = File(updatedDrawing.fileName) //create an empty file file in 'fileDir' special private folder only for the paint app files
            updatedDrawing.bitmap?.let { saveBitmapToFile(it, file)
            } //gives updates to those tracking live data
        }
    }

    // Specifically updating just the filename for a drawing in the database.
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

    suspend fun loadBitmapFromFile(fileName: String): Bitmap? {
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

    suspend fun getAllDrawingsFromServer(): List<Drawing> {
        try {
            val drawings: List<Drawing> = httpClient.get("http://10.0.2.2:8080/drawing/$uId") {
                contentType(io.ktor.http.ContentType.Application.Json)
//                setBody(uId)
            }.body()

            return drawings
        }
        catch (e: Exception) {
            Log.e("DrawRepository", "Error fetching drawings from server", e)
        }
        return emptyList()
    }

    private val defaultBitmap = Bitmap.createBitmap(1080, 2209, Bitmap.Config.ARGB_8888)

    suspend fun getDrawingList(ownerID: String): List<Drawing> {
        try {
                val drawings: List<Drawing> = httpClient.get("http://10.0.2.2:8080/drawings/$ownerID") {
                contentType(ContentType.Application.Json)
            }.body()


            return drawings
        }
        catch (e: Exception) {
            Log.e("DrawRepository", "Error fetching drawings from server", e)
        }
        return emptyList()
    }

    suspend fun getDrawingsListFromServer(): List<Drawing>? {
        return try {
            // Send GET request to the server to fetch drawings
            val response: HttpResponse = httpClient.get("/drawings") {
                accept(ContentType.Application.Json)
            }

            // Check if the request was successful
            if (response.status == HttpStatusCode.OK) {
                // Parse the response JSON into a List of Drawings
                val responseBody = response.bodyAsText()

                //returns a list of drawings sent from the server
                if (responseBody.isNotBlank()) {
                    return Json.decodeFromString(responseBody)
                }
                //users has no drawings on the cloud
                else {
                    emptyList()
                }
            }
            else {
                println("Error: Received status ${response.status}")
                null
            }
        } catch (e: Exception) {
            println("Error fetching drawings: ${e.message}")
            null
        }
    }

    suspend fun downloadDrawing(drawing: Drawing) {
        // Construct the download URL based on your file-saving convention
        val downloadUrl = "http://10.0.2.2:8080/drawing/download/user-${drawing.ownerID}-drawing-${drawing.id}.png"

        try {
            // Download the file bytes from the URL
            val fileBytes = httpClient.get(downloadUrl){
            }.readBytes()

            println("received File")

            // Save the downloaded file locally with its fileName
            withContext(Dispatchers.IO) {
                val localFile = File(drawing.fileName)
                localFile.writeBytes(fileBytes)
            }

            println("finished processing")
        } catch (e: Exception) {
            // Handle any exceptions, like network issues
            println("Failed to download drawing ${drawing.id}: ${e.message}")
            throw e
        }
    }
}

