package com.example.paintapp

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DrawDAO {

    // Query to get the latest drawing (for current drawing use)
    @Query("SELECT * FROM drawing ORDER BY id DESC LIMIT 1")
    fun latestDrawing(): Flow<Drawing>

    // Query to get all drawings stored in the database
    @Query("SELECT * FROM drawing")
    fun allDrawings(): Flow<List<Drawing>>

    // Insert a new drawing
    @Insert
    suspend fun insertDrawing(drawing: Drawing)

    // Update an existing drawing
    @Update
    suspend fun updateDrawing(drawing: Drawing)

    // Delete a specific drawing
    @Delete
    suspend fun deleteDrawing(drawing: Drawing)

    // Get all drawings for suspend functions (if needed elsewhere in the repository)
    @Query("SELECT * FROM drawing")
    suspend fun getAllDrawings(): List<Drawing>
}

//@Dao
//interface DrawDAO {
//
//    @Insert
//    suspend fun addDrawing(data: Drawing)
//
//    @Query("SELECT * FROM Drawing ORDER BY id DESC LIMIT 1")
//    fun latestDrawing(): Flow<Drawing>
//
//    @Query("SELECT * FROM Drawing ORDER BY id DESC")
//    fun allDrawings(): Flow<List<Drawing>>
//}