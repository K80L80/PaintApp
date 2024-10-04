package com.example.paintapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
//TODO: pass DOA 'private val drawDao: DrawDAO'
//TODO: eventually turn this to 'val allDrawings = drawDao.getAllDrawings()' but for testing sake leave it as is
//val allDrawings: LiveData<List<Drawing>> get() =  generateTestDrawingsAsLiveData()
// For testing: replace with actual DAO later

class DrawRepository() {

    private val _allDrawings = MutableLiveData<List<Drawing>>()
    val allDrawings: LiveData<List<Drawing>> get() = _allDrawings

    init {
        // Initialize with some test data or empty list
        _allDrawings.value = generateTestDrawings()
    }

    suspend fun addDrawing(newDrawing: Drawing){
        //Get the current list, adds the new drawing to the end of the list, updates the live data
        val currentList = _allDrawings.value.orEmpty().toMutableList()  //takes the immutable list of drawing and converts it to mutable (ie can edit)
        currentList.add(newDrawing)
        //UI won't freeze waiting for this operation to take place, just will update the main thread when ready
        _allDrawings.postValue(currentList )// uses post value to ensure thread safe if its called from background thread
    }

    suspend fun updateExistingDrawing(updatedDrawing: Drawing ){
        val currentList = _allDrawings.value?.toMutableList() ?: mutableListOf() //

        //Find the drawing in the list that matches this index
        val index = currentList.indexOfFirst { it.id == updatedDrawing.id }

        // Replace the old drawing with the updated one
        currentList[index] = updatedDrawing // Update the drawing in the list

        //gives updates to those tracking live data
        _allDrawings.postValue(currentList)
    }

    suspend fun getAllDrawings() {
        //TODO: drawDao.delete(drawing)

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