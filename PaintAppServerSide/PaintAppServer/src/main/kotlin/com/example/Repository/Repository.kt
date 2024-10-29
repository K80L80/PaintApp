package com.example.Repository

import com.example.plugins.Drawing

//object DrawingRepository {
//    val drawings = mutableListOf<Drawing>()
//
//    fun getDrawingsForUser(userId: Long): List<Drawing> {
//        return drawings.filter { it.ownerID == userId }
//    }
//
//    fun getDrawingById(drawingId: Long): Drawing? {
//        return drawings.find { it.id == drawingId }
//    }

//    fun updateDrawing(drawingId: String, newTitle: String, newContentUrl: String): Drawing? {
//        val drawing = drawings.find { it.drawingId == drawingId } ?: return null
//        val updatedDrawing = drawing.copy(
//            imageTitle = newTitle,
//            contentUrl = newContentUrl,
//            updatedAt = System.currentTimeMillis(),
//            version = drawing.version + 1  // Increment the version on update
//        )
//        drawings[drawings.indexOf(drawing)] = updatedDrawing
//        return updatedDrawing
//    }
//}