package com.example.paintapp

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.Dao
import kotlinx.coroutines.flow.Flow


@Dao
interface DrawDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addDrawing(drawingEntity: DrawEntity) : Long //returns the id of the newly inserted record

    @Update
    suspend fun updateDrawing(drawingEntity: DrawEntity)

    @Delete
    suspend fun deleteDrawing(drawingEntity: DrawEntity)

    @Query("SELECT * FROM drawings")
    fun getAllDrawings(): Flow<List<DrawEntity>>
}