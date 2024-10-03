package com.example.paintapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
//TODO: pass DOA 'private val drawDao: DrawDAO'
class DrawRepository() {

    val allDrawings: LiveData<List<Drawing>> = generateTestDrawingsAsLiveData()
    //TODO: val allDrawings = drawDao.getAllDrawings()

    suspend fun getAllDrawings() {
        //TODO: drawDao.delete(drawing)
    }
    suspend fun addDrawingToList(){

    }
    suspend fun insertDrawing(drawing: Drawing) {
        //TODO: drawDao.insert(drawing)
    }

    suspend fun updateDrawing(drawing: Drawing) {
       //TODO: drawDao.update(drawing)
    }

    suspend fun deleteDrawing(drawing: Drawing) {
       //TODO: drawDao.delete(drawing)
    }

}