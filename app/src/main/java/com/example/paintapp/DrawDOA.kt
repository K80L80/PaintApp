package com.example.paintapp

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DrawDAO {

    @Insert
    suspend fun addDrawing(data: DrawEntity)

    @Query("SELECT * FROM Drawings ORDER BY id DESC LIMIT 1")
    fun latestDrawing(): Flow<DrawEntity>

    @Query("SELECT * FROM Drawings ORDER BY id DESC")
    fun allDrawings(): Flow<List<DrawEntity>>
}