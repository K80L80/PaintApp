package com.example.paintapp

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

//-----------Application (Singleton Pattern)----------------//
//Application class is used to create a global instance of objects like a repository or database, ensuring that only one instance is created and shared across the entire app.
class DrawApp : Application() {
    val scope = CoroutineScope(SupervisorJob()) //eager – ready to launch , use supervisor job allows a child routine to fail without bring everything else down
    val db by lazy { DrawDatabase.getDatabase(applicationContext) }//lazy – defer database creation till you need it, applicationContext in Android refers to a global context tied to the entire lifecycle of the app (applicationContext isn't tied to any specific screen or component.)
    val drawRepository by lazy { DrawRepository(scope, db.drawDao(), applicationContext) } //lazy – defer repository creation till you need it
}


//Notes: //In Android, every app has a default Application object created automatically by the system.
//// This object represents the entire app's global state and is accessible from any part of the app using applicationContext.
////You can subclass the Application class to create your own custom version (like WeatherApplication).