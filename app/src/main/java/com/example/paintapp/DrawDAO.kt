package com.example.paintapp

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.Dao
import kotlinx.coroutines.flow.Flow


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