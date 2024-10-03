package com.example.paintapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.lifecycle.asLiveData
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.commons.io.output.ByteArrayOutputStream
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.io.InputStreamReader

class DrawRepository(val scope: CoroutineScope, val dao: DrawDAO, val context: Context) {

    val currentDrawing = dao.latestDrawing().asLiveData()
    val allDrawings = dao.allDrawings().asLiveData()

    // Get all drawings from the Room database
    suspend fun getAllDrawings(): List<Drawing> {
        return dao.getAllDrawings()
    }

    // Insert a new drawing into the database
    suspend fun addDrawing(drawing: Drawing) {
        dao.insertDrawing(drawing)
    }

    // Update a drawing in the database
    suspend fun updateDrawing(drawing: Drawing) {
        dao.updateDrawing(drawing)
    }

    // Delete a drawing from the database
    suspend fun deleteDrawing(drawing: Drawing) {
        dao.deleteDrawing(drawing)
    }

    // Load the bitmap from a file path
    fun loadBitmapFromPath(path: String): Bitmap {
        // Implement logic to load a bitmap from the provided file path
        return BitmapFactory.decodeFile(path) // Example implementation
    }

    fun updateDrawingInList(updatedDrawing: Drawing) {
        scope.launch(Dispatchers.IO) {
            // Fetch all drawings directly from the DAO (in a background thread)
            val currentList = dao.getAllDrawings().toMutableList() // Fetches list from Room DB

            val index = currentList.indexOfFirst { it.id == updatedDrawing.id } // No longer unresolved

            if (index != -1) {
                currentList[index] = updatedDrawing
                dao.updateDrawing(updatedDrawing) // Update the drawing in the DB
            }
        }
    }

    fun saveBitmapToFile(bitmap: Bitmap, fileName: String): String {
        // Define the directory where the bitmap will be saved
        val directory = File(context.filesDir, "bitmaps")
        if (!directory.exists()) {
            directory.mkdirs() // Create directory if it doesn't exist
        }

        // Create the file for the bitmap
        val file = File(directory, "$fileName.png")

        try {
            FileOutputStream(file).use { out ->
                // Compress the bitmap and save it as a PNG file
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        // Return the absolute path of the saved file
        return file.absolutePath
    }
//
//    private val gson = Gson()
//    val bitmapPath = "app/src/main/java/com/example/paintapp/bitmaps.json"
//
//   //this saves all drawings from our file.
//    fun loadDrawingFromFile(fileName: String) {
//        scope.launch {
//            val allBitmaps = readDrawingFile(fileName)
//            if (allBitmaps != null) {
//                for (drawing in allBitmaps) {
//                    dao.addDrawing(drawing)
//                }
//            }
//        }
//    }
//
//    /**
//     * Function to read the drawing from a file
//     */
//    private suspend fun readDrawingFile(fileName: String): List<DrawEntity>? {
//        return withContext(Dispatchers.IO) {
//            try {
//                val bitmapFile = File(context.filesDir, fileName)
//                if (bitmapFile.exists()) {
//                    val bitmapInputStream = FileInputStream(bitmapFile)
//                    val bitmapReader = InputStreamReader(bitmapInputStream, "UTF-8")
//                    val bitmapsFiles = bitmapReader.readText()
//                    val bitmapDrawings =
//                        Gson().fromJson(bitmapsFiles, Array<DrawEntity>::class.java).toList()
//                    bitmapDrawings
//                }
//                else
//                {
//                    null
//                }
//            }
//            catch (e: Exception) {
//                e.printStackTrace()
//                null
//            }
//        }
//    }
//
//    /**function to save a bitmap to our created file.
//     *
//     */
//    fun saveBitmapToFile(drawing: Drawing) {
//        //get the current file path or create the file
//        val drawingFileName = "${drawing.id}.json"
//
//        val bitmapFile = getBitmapPath(bitmapPath)
//        //get our bitmap to save.
//        val bitmap = BitmapData(bitmapName, bitmapToBase64(bitmap))
//
//        //first try to read the file
//        try {
//            val reader = FileReader(bitmapFile)
//            var bitmapList: MutableList<BitmapData>
//            try
//            {
//                val fileType = object : TypeToken<MutableList<BitmapData>>() {}.type
//                bitmapList = gson.fromJson(reader, fileType) ?: mutableListOf()
//            }
//            finally
//            {
//                reader.close()
//            }
//
//            bitmapList.add(bitmap)
//
//            //now add bitmap and write.
//            val writer = FileWriter(bitmapFile)
//            try
//            {
//                gson.toJson(bitmapList, writer)
//            }
//            finally
//            {
//                writer.close()
//            }
//
//            Log.d("File IO DVM", "Bitmap saved")
//        } catch (e: IOException) {
//            Log.e("File IO DVM", "Error writing bitmap")
//        }
//    }
//
//    /**Method to convert bitmap for our file
//     *
//     */
//    private fun bitmapToBase64(bitmap: Bitmap): String {
//        val outputStream = ByteArrayOutputStream()
//        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
//        return android.util.Base64.encodeToString(outputStream.toByteArray(), android.util.Base64.DEFAULT)
//    }
//
//    /**Function to check if file already exists, and return the path
//     * or create the file and return that path.
//     *
//     */
//    private fun getBitmapPath(filePath: String): File {
//        val bitmapFile = File(filePath)
//        if (!bitmapFile.exists()) {
//            try {
//                bitmapFile.createNewFile()
//                val writer = FileWriter(bitmapFile)
//                try
//                {
//                    val emptyList = mutableListOf<BitmapData>()
//                    gson.toJson(emptyList, writer)
//                }
//                finally
//                {
//                    writer.close()
//                }
//            }
//            catch (e: IOException)
//            {
//                Log.e("Bitmap File IO", "Error creating file")
//            }
//        }
//        return bitmapFile
//    }
}