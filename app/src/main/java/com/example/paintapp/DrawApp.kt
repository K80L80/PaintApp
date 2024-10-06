package com.example.paintapp

import android.app.Application
import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

//-----------Application (Singleton Pattern)----------------//
//Application class is used to create a global instance of objects like a repository or database, ensuring that only one instance is created and shared across the entire app.
class DrawApp : Application() {
    val scope = CoroutineScope(SupervisorJob()) //eager – ready to launch , use supervisor job allows a child routine to fail without bring everything else down
    val db by lazy { DrawDatabase.getDatabase(applicationContext) }//lazy – defer database creation till you need it, applicationContext in Android refers to a global context tied to the entire lifecycle of the app (applicationContext isn't tied to any specific screen or component.)
    val drawRepository by lazy { DrawRepository(scope, db.drawDao(), applicationContext) } //lazy – defer repository creation till you need it

//    override fun onCreate() {
//        super.onCreate()
//
//        // Perform both file and database cleanup
//        scope.launch {
//            // Delay to ensure app components are ready
//            delay(5000)
//
//            // Clear the Room database
//            clearDatabase()
//
//            // Delete all files in the filesDir
//            deleteAllFilesFromFilesDir(this@DrawApp)
//        }
//    }
//
//    private suspend fun clearDatabase() {
//        // Clear all tables in the Room database
//        db.clearAllTables()
//    }
//
//    private fun deleteAllFilesFromFilesDir(context: Context): Boolean {
//        val filesDir = context.filesDir
//        if (filesDir.isDirectory) {
//            val files = filesDir.listFiles()
//            if (files != null) {
//                for (file in files) {
//                    if (!file.delete()) {
//                        // If a file could not be deleted, return false
//                        return false
//                    }
//                }
//            }
//        }
//        // Return true if all files were successfully deleted
//        return true
//    }

}




//Notes: //In Android, every app has a default Application object created automatically by the system.
//// This object represents the entire app's global state and is accessible from any part of the app using applicationContext.
////You can subclass the Application class to create your own custom version (like WeatherApplication).