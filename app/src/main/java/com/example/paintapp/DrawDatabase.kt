package com.example.paintapp
import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

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

@Dao
interface DrawDAO {

    @Insert
    suspend fun addDrawing(data: DrawEntity)

    @Query("SELECT * FROM Drawings ORDER BY id DESC LIMIT 1")
    fun latestDrawing(): Flow<DrawEntity>

    @Query("SELECT * FROM Drawings ORDER BY id DESC")
    fun allDrawings(): Flow<List<DrawEntity>>
}