package com.example.paintapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
//TODO: pass DOA 'private val drawDao: DrawDAO'
class DrawRepository() {

    private val _drawings: LiveData<List<Drawing>> = generateTestDrawingsAsLiveData()
    val allDrawings: LiveData<List<Drawing>> get() = _drawings
    //TODO: val allDrawings = drawDao.getAllDrawings()

    suspend fun getAllDrawings() {
        //TODO: drawDao.delete(drawing)

    }
    suspend fun addDrawing(newDrawing: Drawing){
        val currentDrawings = _drawings.value ?: emptyList()
        _drawings.value = currentDrawings + newDrawing
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