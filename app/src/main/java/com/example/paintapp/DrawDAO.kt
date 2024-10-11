package com.example.paintapp

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.Dao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale


@Dao
interface DrawDAO {
    @Insert
    suspend fun addDrawing(drawingEntity: DrawEntity) : Long //returns the id of the newly inserted record

    @Update
    suspend fun updateDrawing(drawingEntity: DrawEntity)

    @Delete
    suspend fun deleteDrawing(drawingEntity: DrawEntity)

    @Query("SELECT * FROM drawings WHERE id IN (SELECT MAX(id) FROM drawings GROUP BY fileName) ORDER BY id DESC")
    fun getAllDrawings(): Flow<List<DrawEntity>>


    @Query("SELECT * FROM drawings WHERE fileName = :fileName LIMIT 1")
    suspend fun getDrawingByFileName(fileName: String): DrawEntity?

}

// Function to print the contents of the Drawing table
fun printDrawingTable(dao: DrawDAO) {
    CoroutineScope(Dispatchers.IO).launch {
        val drawings = dao.getAllDrawings().first()

        if (drawings.isEmpty()) {
            Log.d("Database", "No drawings found in the table.")
            return@launch
        }

        val header = String.format("%-10s%-30s%-50s", "ID", "Name", "Path")
        val divider = "-".repeat(90)

        withContext(Dispatchers.Main) {
            Log.d("Database", header)
            Log.d("Database", divider)

            for (drawing in drawings) {
                val formattedRow = String.format(
                    Locale.US,
                    "%-10d%-30s",
                    drawing.id,
                    drawing.fileName ?: "unknown filename"
                )
                Log.d("Database", formattedRow)
            }
        }
    }
}