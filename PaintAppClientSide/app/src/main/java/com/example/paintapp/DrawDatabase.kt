package com.example.paintapp
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


@Database(entities = [DrawEntity::class], version = 2, exportSchema = false)
abstract class DrawDatabase : RoomDatabase() {
    abstract fun drawDao(): DrawDAO

    companion object {
        @Volatile
        private var INSTANCE: DrawDatabase? = null

        fun getDatabase(context: Context): DrawDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DrawDatabase::class.java,
                    "DrawingsDB"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}