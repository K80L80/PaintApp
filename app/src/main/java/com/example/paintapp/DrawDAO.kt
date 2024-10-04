package com.example.paintapp

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.Dao


@Dao
interface DrawDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addDrawing(drawingEntity: DrawEntity)

    @Update
    suspend fun updateDrawing(drawingEntity: DrawEntity)

    @Delete
    suspend fun deleteDrawing(drawingEntity: DrawEntity)

    @Query("SELECT * FROM drawings")
    fun getAllDrawings(): LiveData<List<DrawEntity>>
}