package com.example.paintapp

import android.content.Context
import androidx.lifecycle.asLiveData
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader

class DrawRepository(val scope: CoroutineScope, val dao: DrawDAO, val context: Context) {

    val currentDrawing = dao.latestDrawing().asLiveData()
    val allDrawings = dao.allDrawings().asLiveData()

   //this saves all drawings from our file.
    fun loadDrawingFromFile(fileName: String) {
        scope.launch {
            val allBitmaps = readDrawingFile(fileName)
            if (allBitmaps != null) {
                for (drawing in allBitmaps) {
                    dao.addDrawing(drawing)
                }
            }
        }
    }

    /**
     * Function to read the drawing from a file
     */
    private suspend fun readDrawingFile(fileName: String): List<DrawEntity>? {
        return withContext(Dispatchers.IO) {
            try {
                val bitmapFile = File(context.filesDir, fileName)
                if (bitmapFile.exists()) {
                    val bitmapInputStream = FileInputStream(bitmapFile)
                    val bitmapReader = InputStreamReader(bitmapInputStream, "UTF-8")
                    val bitmapsFiles = bitmapReader.readText()
                    val bitmapDrawings =
                        Gson().fromJson(bitmapsFiles, Array<DrawEntity>::class.java).toList()
                    bitmapDrawings
                }
                else
                {
                    null
                }
            }
            catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}